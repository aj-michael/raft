package edu.rosehulman.client

import edu.rosehulman.collections.DistributedMap

import scala.collection.mutable.Map

case class Profile(name: String, email: String, creditcard: String)

object Application extends App {
  val workers = List(
    "akka.tcp://raft@127.0.0.1:2553/user/worker",
    "akka.tcp://raft@127.0.0.1:2554/user/worker",
    "akka.tcp://raft@127.0.0.1:2555/user/worker"
  )
  val map: Map[String, Profile] = new DistributedMap[Profile](workers)
  map += "adam" -> Profile("Adam Michael", "adam@ajmichael.net", "234567890")
  println("OK Trying get")
  println(map.get("adam"))
}
