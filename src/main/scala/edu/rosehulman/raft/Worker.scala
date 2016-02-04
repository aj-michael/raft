package edu.rosehulman.raft

import akka.actor.ActorSystem

object Worker extends App {
  val system = ActorSystem("raft")
  val clusterSize = system.settings.config.getList("akka.cluster.seed-nodes").size
  val ref = system.actorOf(RaftActor.props(clusterSize), "worker")
}
