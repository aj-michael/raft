package edu.rosehulman.raft.messages

import edu.rosehulman.raft.State.{RaftId, LogEntry}

object CommandRequest {
  def get(key: String) = CommandRequest((m: Map[String, Any]) => (m.get(key), m))
  def put(key: String, value: Any) = CommandRequest((m: Map[String, Any]) => (true, m + (key -> value)))
  def delete(key: String) = CommandRequest((m: Map[String, Any]) => (m.contains(key), m - key))
  def iterator = CommandRequest((m: Map[String, Any]) => (m.iterator, m))
}

final case class CommandRequest private(entry: LogEntry)

final case class FailedCommandResponse(leaderId: Option[RaftId])
final case class CommandResponse(result: Any)
