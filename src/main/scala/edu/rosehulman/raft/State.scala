package edu.rosehulman.raft

sealed trait State {
  import State.CandidateId
  import State.Index
  import State.Term

  val currentTerm: Term
  val votedFor: Option[CandidateId]
  val log: Map[Index, LogEntry]
  val commitIndex: Index
  val lastApplied: Index
}

object State {
  type CandidateId = Int
  type Index = Int
  type Term = Int

  final case class Follower(
    currentTerm: Term,
    votedFor: Option[CandidateId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index
  ) extends State {
    def this() = this(0, Option.empty, Map(), 0, 0)
  }

  final case class Candidate(
    currentTerm: Term,
    votedFor: Option[CandidateId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index
  ) extends State

  final case class Leader(
    currentTerm: Term,
    votedFor: Option[CandidateId],
    log: Map[Index, LogEntry],
    commitIndex: Index,
    lastApplied: Index
  ) extends State
}