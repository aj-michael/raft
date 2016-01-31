package edu.rosehulman.raft.messages

final case class RequestVoteResponse(
  term: Int,
  voteGranted: Boolean
)
