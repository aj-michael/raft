package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.CandidateId
import edu.rosehulman.raft.State.Index
import edu.rosehulman.raft.State.Term

final case class RequestVoteRequest(
  term: Term,
  candidateId: CandidateId,
  lastLogIndex: Index,
  lastLogTerm: Index
)
