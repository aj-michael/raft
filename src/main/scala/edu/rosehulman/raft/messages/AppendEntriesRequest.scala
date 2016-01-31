package edu.rosehulman.raft.messages

case class AppendEntriesRequest(
  term: Int,
  leaderId: Int,
  prevLogIndex: Int,
  prevLogTerm: Int,
  entries: Array[String]
)