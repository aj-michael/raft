package edu.rosehulman.raft

import akka.actor.{Props, Actor, FSM}
import edu.rosehulman.raft.messages.{RequestVoteResponse, RequestVoteRequest, AppendEntriesRequest}
import scala.concurrent.duration._

object RaftActor {
  def props(): Props = Props(new RaftActor)
}

class RaftActor() extends Actor with FSM[Role, State] {

  startWith(Role.Follower, new State.Follower())

  when(Role.Follower, stateTimeout = 150 milliseconds) {
    case Event(StateTimeout, s: State.Follower) =>
      stay
    case Event(AppendEntriesRequest, s: State.Follower) =>
      stay
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), s: State.Follower) =>
      val vote = term >= s.currentTerm && s.votedFor.isEmpty
      sender() ! RequestVoteResponse(s.currentTerm, vote)
      stay
  }

  when(Role.Candidate) {
    case Event(RequestVoteResponse(term, vote), s: State.Candidate) =>
      stay using s
  }

  when(Role.Leader) {
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), s: State.Leader) =>
      stay using s
  }

  whenUnhandled {
    case Event(e, s) =>
      log.warning("Received unhandled request {}", e)
      stay
  }

  initialize()
}
