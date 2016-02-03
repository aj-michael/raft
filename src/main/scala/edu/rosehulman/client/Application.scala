package edu.rosehulman.client

import akka.actor.ActorSystem
import edu.rosehulman.raft.RaftActor
import edu.rosehulman.raft.messages.RequestVoteRequest

object Application {
  def main(args: Array[String]) = {
    println("Attempting to create RaftActor")
    val system = ActorSystem("raft")
    val ref = system.actorOf(RaftActor.props)
    ref ! RequestVoteRequest
  }
}
