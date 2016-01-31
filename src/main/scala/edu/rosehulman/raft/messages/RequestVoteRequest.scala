package edu.rosehulman.raft.messages

case class RequestVoteRequest(
  term: Int,
  candidateId: Int,
  lastLogIndex: Int,
  lastLogTerm: Int
)
