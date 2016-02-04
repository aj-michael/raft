package edu.rosehulman.client

import akka.actor._
import com.typesafe.config.ConfigValue
import edu.rosehulman.collections.DistributedMap
import edu.rosehulman.raft.RaftActor
import edu.rosehulman.raft.messages.CommandRequest

import scala.collection.JavaConverters._

case class Profile(name: String, email: String, creditcard: String)

object Application extends App {
  /*val system = ActorSystem("raft")
  val actors = system.settings.config.getList("akka.cluster.seed-nodes").asScala
    .map(_.unwrapped + "/user/worker").map(system.actorSelection(_))
  actors.foreach { a =>
    a ! CommandRequest.put("abc", "def")
  }*/
  val workers = List(
    "akka.tcp://raft@127.0.0.1:2553/user/worker",
    "akka.tcp://raft@127.0.0.1:2554/user/worker",
    "akka.tcp://raft@127.0.0.1:2555/user/worker"
  )
  val map: DistributedMap[Profile] = new DistributedMap[Profile](workers)
  map += "adam" -> Profile("Adam Michael", "adam@ajmichael.net", "234567890")
}
