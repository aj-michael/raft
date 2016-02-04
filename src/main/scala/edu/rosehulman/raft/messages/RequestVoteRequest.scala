package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.{RaftId, Index, Term}

final case class RequestVoteRequest(
  term: Term,
  candidateId: RaftId,
  lastLogIndex: Index,
  lastLogTerm: Index
)
