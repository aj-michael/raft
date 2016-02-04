package edu.rosehulman.collections

import akka.actor._
import akka.event.Logging
import akka.event.slf4j.Logger
import akka.pattern.ask
import akka.util.Timeout
import edu.rosehulman.raft.messages.{CommandResponse, FailedCommandResponse, CommandRequest}

import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.Breaks._

class DistributedMap[T] (raftWorkerPaths: List[String]) extends scala.collection.mutable.Map[String, T] {
  val system = ActorSystem("raft")
  val log = Logging.getLogger(system, this)
  val workers: List[ActorSelection] = raftWorkerPaths.map(system.actorSelection(_))
  implicit val timeout = Timeout(10.seconds)

  override def +=(kv: (String, T)) = {
    breakable {
      workers.foreach { a =>
        val future = a ? CommandRequest.put(kv._1, kv._2)
        try
          Await.result(future, timeout.duration) match {
            case FailedCommandResponse(leaderId) =>
              if (leaderId.isDefined) {
                val ref = system.actorSelection(RootActorPath(leaderId.get) / "user" / "worker")
              }
            case CommandResponse(value) =>
              log.info("Command += returned value: " + value)
              break
          }
        catch {
          case TimeoutException => log.warning("Worker " + a + " is not responding")
        }
      }
    }
    this
  }

  override def -=(key: String) = {
    workers.foreach { a =>
      a ! CommandRequest.delete(key)
    }
    this
  }

  override def get(key: String) = {
    workers.foreach { a =>
      a ! CommandRequest.get(key)
    }
    Option.empty
  }

  override def iterator = {
    workers.foreach { a =>
      a ! CommandRequest.iterator
    }
    Iterator.empty
  }
}
