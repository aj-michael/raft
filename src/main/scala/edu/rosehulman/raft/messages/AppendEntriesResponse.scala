package edu.rosehulman.raft.messages

case class AppendEntriesResponse(
  term: Int,
  success: Boolean
)
