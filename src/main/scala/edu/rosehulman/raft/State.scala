package edu.rosehulman.raft

sealed trait State {
  import State.CandidateId
  import State.Index

  val currentTerm: Int
  val votedFor: Option[CandidateId]
  val log: Map[Index, LogEntry]
  val commitIndex: Index
  val lastApplied: Index
}

object State {
  type CandidateId = Int
  type Index = Int

  final case class Follower(
    currentTerm: Int,
    votedFor: Option[Int],
    log: Map[Index, LogEntry],
    commitIndex: Int,
    lastApplied: Int
  ) extends State {
    def this() = this(0, Option.empty, Array(), 0, 0)
  }

  final case class Candidate(
    currentTerm: Int,
    votedFor: Option[Int],
    log: Array[LogEntry],
    commitIndex: Int,
    lastApplied: Int
  ) extends State

  final case class Leader(
    currentTerm: Int,
    votedFor: Int,
    log: Array[LogEntry],
    commitIndex: Int,
    lastApplied: Int
  ) extends State
}