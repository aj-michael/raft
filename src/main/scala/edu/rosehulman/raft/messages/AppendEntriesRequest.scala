package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.CandidateId
import edu.rosehulman.raft.State.Index
import edu.rosehulman.raft.State.Term

case class AppendEntriesRequest(
  term: Term,
  leaderId: CandidateId,
  prevLogIndex: Index,
  prevLogTerm: Index,
  entries: Array[String]
)