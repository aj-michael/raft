package edu.rosehulman.raft

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import edu.rosehulman.raft.State._
import edu.rosehulman.raft.messages._
import scala.concurrent.duration._
import scala.util.Random

object RaftActor {
  def props(numPeers: Int): Props = Props(new RaftActor(numPeers))
}

class RaftActor(clusterSize: Int) extends Actor with LoggingFSM[Role, State] {

  val cluster = Cluster(context.system)

  override def preStart = {
    cluster.subscribe(self, classOf[MemberUp])
  }

  def electionTimeout = {
    (300 + new Random(System.currentTimeMillis).nextInt(150)).milliseconds
  }
  def heartbeatTimeout = 100.milliseconds

  startWith(Role.StartingUp, Uninitialized(cluster.selfAddress, Set()))

  when(Role.StartingUp) {
    case Event(MemberUp(m), s: Uninitialized) =>
      m.address.equals(self.path.address)
      if (m.address.equals(cluster.selfAddress)) {
        stay
      } else if (s.peers.size == clusterSize - 2) {
        goto(Role.Follower) using Follower.create(s.id, s.peers + m.address) forMax electionTimeout
      } else {
        stay using Uninitialized(s.id, s.peers + m.address)
      }
  }

  when(Role.Follower) {
    case Event(StateTimeout, s: Follower) =>
      goto(Role.Candidate) using Candidate.create(s) forMax electionTimeout
    case Event(AppendEntriesRequest(term, leaderId, prevLogIndex, prevLogTerm, entries), s: Follower) =>
      sender ! AppendEntriesResponse(s.currentTerm, true)
      stay using s.addEntry(entries.lastOption) forMax electionTimeout
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), s: Follower) =>
      if (term >= s.currentTerm && s.votedFor.isEmpty) {
        log.info("Voting for " + candidateId + " in term " + term)
        sender ! RequestVoteResponse(term, true)
        stay using s.voteFor(term, candidateId) forMax electionTimeout
      } else {
        log.info("Voting against " + candidateId + " in term " + term)
        sender ! RequestVoteResponse(term, false)
        stay forMax electionTimeout
      }
  }

  when(Role.Candidate) {
    case Event(StateTimeout, s: Candidate) =>
      goto(Role.Candidate) using Candidate.create(s) forMax electionTimeout
    case Event(RequestVoteResponse(term, vote), s: Candidate) =>
      if (vote && (s.votes.groupBy(_._2).get(true).getOrElse(Map()).size + 1) * 2 > clusterSize) {
        goto(Role.Leader) using Leader.create(s)
      } else {
        stay using s.addVote(sender.path.address, vote) forMax electionTimeout
      }
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), s: Candidate) =>
      if (term > s.currentTerm) {
        log.info("Voting for " + candidateId + " in term " + term)
        sender ! RequestVoteResponse(term, true)
        goto(Role.Follower) using s.voteFor(term, candidateId) forMax electionTimeout
      } else {
        log.info("Voting against " + candidateId + " in term " + term)
        sender ! RequestVoteResponse(term, false)
        stay forMax electionTimeout
      }
    case Event(AppendEntriesRequest(term, leaderId, prevLogIndex, prevLogTerm, entries), s: Candidate) =>
      if (term > s.currentTerm) {
        goto(Role.Follower) using s.addEntry(entries.last) forMax electionTimeout
      } else {
        stay forMax electionTimeout
      }
  }

  when(Role.Leader, stateTimeout = heartbeatTimeout) {
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), s: Leader) =>
      stay using s
    case Event(StateTimeout, s: Leader) =>
      s.peers.foreach(actorFor(_) ! AppendEntriesRequest(s.currentTerm, s.id, s.lastApplied, s.currentTerm, Array()))
      stay
    case Event(AppendEntriesResponse(term, success), s: Leader) =>
      stay
    case Event(AppendEntriesRequest(term, leaderId, prevLogIndex, prevLogTerm, entries), s: Candidate) =>
      if (term > s.currentTerm) {
        goto(Role.Follower) using s.addEntry(entries.last) forMax electionTimeout
      } else {
        stay
      }
  }

  whenUnhandled {
    case Event(e, s) =>
      log.warning("Received unhandled request {}", e)
      stay
  }

  onTransition {
    case (Role.Follower|Role.Candidate) -> Role.Candidate =>
      nextStateData match {
        case s: RaftState =>
          log.info("Requesting votes for term " + s.currentTerm)
          s.peers.foreach(actorFor(_) ! RequestVoteRequest(s.currentTerm, s.id, s.commitIndex, s.lastApplied))
        case s: Uninitialized =>
          log.error("Illegal state transition detected")
      }

  }

  def actorFor(address: Address) = {
    context.actorSelection(RootActorPath(address) / "user" / "worker")
  }

  initialize()
}
