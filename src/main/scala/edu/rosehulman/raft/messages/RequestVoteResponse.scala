package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.Term

final case class RequestVoteResponse(
  term: Term,
  voteGranted: Boolean
)
