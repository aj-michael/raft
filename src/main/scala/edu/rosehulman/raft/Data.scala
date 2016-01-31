package edu.rosehulman.raft

trait Data

case class NonleaderData(
  currentTerm: Int,
  votedFor: Option[Int],
  log: Array[LogEntry],
  commitIndex: Int,
  lastApplied: Int
) extends Data {
  def this() = this(0, Option.empty, Array(), 0, 0)
}

case class LeaderData(
  currentTerm: Int,
  votedFor: Int,
  log: Array[LogEntry],
  commitIndex: Int,
  lastApplied: Int
) extends Data