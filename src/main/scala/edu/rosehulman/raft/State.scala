package edu.rosehulman.raft

import akka.actor.Address
import edu.rosehulman.raft.State.{Follower, RaftId}

sealed trait State {
  val id: RaftId
  val peers: Set[RaftId]
}

case class Uninitialized(
  id: RaftId,
  peers: Set[RaftId]
) extends State

sealed trait RaftState extends State {
  import State.Index
  import State.LogEntry
  import State.RaftId
  import State.Term

  val currentTerm: Term
  val votedFor: Option[RaftId]
  val log: Map[Index, LogEntry]
  val commitIndex: Index
  val lastApplied: Index
  val id: RaftId
  val peers: Set[RaftId]

  def addEntry(entry: LogEntry) = Follower(currentTerm, votedFor,
    log + (lastApplied + 1 -> entry), commitIndex, lastApplied + 1, id, peers)

  def addEntry(entry: Option[LogEntry]): Follower = if (entry.isDefined) addEntry(entry.get) else Follower.create(this)
}

object State {
  type RaftId = Address
  type Index = Int
  type Term = Int
  type LogEntry = String

  object Follower {
    def create(id: RaftId, peers: Set[RaftId]) = Follower(1, Option.empty, Map(), 0, 0, id, peers)
    def create(s: RaftState) = Follower(s.currentTerm, s.votedFor, s.log, s.commitIndex,
      s.lastApplied, s.id, s.peers)
  }

  final case class Follower(
    currentTerm: Term,
    votedFor: Option[RaftId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index,
    id: RaftId,
    peers: Set[RaftId]
  ) extends RaftState {
    def voteFor(term: Term, candidateId: RaftId) = Follower(term, Option(candidateId), log,
      commitIndex, lastApplied, id, peers)
  }

  object Candidate {
    def create(s: RaftState) = Candidate(s.currentTerm + 1, Option(s.id), s.log, s.commitIndex,
      s.lastApplied, s.id, s.peers, Map(s.id -> true))
  }

  final case class Candidate(
    currentTerm: Term,
    votedFor: Option[RaftId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index,
    id: RaftId,
    peers: Set[RaftId],
    votes: Map[RaftId, Boolean]
  ) extends RaftState {
    def addVote(voterId: RaftId, vote: Boolean) = Candidate(currentTerm, votedFor, log, commitIndex,
      lastApplied, id, peers, votes + (voterId -> vote))
    def voteFor(term: Term, candidateId: RaftId) = Follower(term, Option(candidateId), log,
      commitIndex, lastApplied, id, peers)
  }

  object Leader {
    def create(c: Candidate) = this(c.currentTerm, Option.empty, c.log, c.commitIndex, c.lastApplied, c.id, c.peers)
  }

  final case class Leader(
    currentTerm: Term,
    votedFor: Option[RaftId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index,
    id: RaftId,
    peers: Set[Address]
  ) extends RaftState
}
