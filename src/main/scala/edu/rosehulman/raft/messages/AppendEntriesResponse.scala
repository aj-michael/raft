package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.Term

case class AppendEntriesResponse(
  term: Term,
  success: Boolean
)
