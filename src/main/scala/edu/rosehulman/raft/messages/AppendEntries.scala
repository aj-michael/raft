package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.{LogEntry, Index, RaftId, Term}

case class AppendEntriesRequest(
  term: Term,
  leaderId: RaftId,
  prevLogIndex: Index,
  prevLogTerm: Index,
  entries: Array[LogEntry],
  leaderCommit: Index
)

case class AppendEntriesResponse(
  term: Term,
  success: Boolean
)
