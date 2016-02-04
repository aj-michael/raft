package edu.rosehulman.client

import akka.actor._
import akka.event.Logging
import akka.remote.RemoteScope
import edu.rosehulman.raft.RaftActor
import edu.rosehulman.raft.messages.RequestVoteRequest
import scala.collection.JavaConverters._
import scala.collection.mutable

object Application {
  val system = ActorSystem("raft")
  val log = Logging.getLogger(system, this)

  def main(args: Array[String]) = {
    //val ref = system.actorOf(RaftActor.props)
    //val workers = system.settings.config.getList("raft.workers").unwrapped.asScala.map(_.toString).map(
    //  s => system.actorOf(Props[RaftActor].withDeploy(Deploy(scope = RemoteScope(AddressFromURIString(s))))))
    //log.info("Created actor at " + ref.path)
    //ref ! RequestVoteRequest(1, 1, 1, 1)
    val numPeers = 3
    val ref = system.actorOf(RaftActor.props(numPeers), "worker")
  }
}
