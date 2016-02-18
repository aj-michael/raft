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

  override def +=(kv: (String, T)): this.type = {
    val request = CommandRequest.put(kv._1, kv._2)
    workers.foreach { a =>
      val future = a ? request
      try
        Await.result(future, timeout.duration) match {
          case FailedCommandResponse(leaderId) =>
            if (leaderId.isDefined) {
              val ref = system.actorSelection(RootActorPath(leaderId.get) / "user" / "worker")
              Await.result(ref ? request, timeout.duration) match {
                case FailedCommandResponse(leaderId) =>
                  throw new RuntimeException("Leader is down, all hope is lost: " + leaderId)
                case CommandResponse(value) =>
                  log.info("Command += returned value: " + value)
                  return this
              }
            }
          case CommandResponse(value) =>
            log.info("Command += returned value: " + value)
            return this
        }
      catch {
        case e: TimeoutException => log.warning("Worker " + a + " is not responding")
      }
    }
    return this
  }

  override def -=(key: String) = {
    val request = CommandRequest.delete(key)
    breakable {
      workers.foreach { a =>
        val future = a ? request
        try
          Await.result(future, timeout.duration) match {
            case FailedCommandResponse(leaderId) =>
              if (leaderId.isDefined) {
                val ref = system.actorSelection(RootActorPath(leaderId.get) / "user" / "worker")
                Await.result(ref ? request, timeout.duration) match {
                  case FailedCommandResponse(leaderId) =>
                    throw new RuntimeException("Leader is down, all hope is lost")
                  case CommandResponse(value) =>
                    log.info("Command -= returned value: " + value)
                    break
                }
              }
            case CommandResponse(value) =>
              log.info("Command -= returned value: " + value)
              break
          }
        catch {
          case e: TimeoutException => log.warning("Worker " + a + " is not responding")
        }
      }
    }
    this
  }

  override def get(key: String) = {
    val request = CommandRequest.get(key)
    var returnValue: Option[T] = Option.empty
    breakable {
      workers.foreach { a =>
        val future = a ? request
        try
          Await.result(future, timeout.duration) match {
            case FailedCommandResponse(leaderId) =>
              if (leaderId.isDefined) {
                val ref = system.actorSelection(RootActorPath(leaderId.get) / "user" / "worker")
                Await.result(ref ? request, timeout.duration) match {
                  case FailedCommandResponse(leaderId) =>
                    throw new RuntimeException("Leader is down, all hope is lost")
                  case CommandResponse(value: T) =>
                    log.info("Command get returned value: " + value)
                    returnValue = Option(value)
                    break
                }
              }
            case CommandResponse(value: T) =>
              log.info("Command get returned value: " + value)
              returnValue = Option(value)
              break
          }
        catch {
          case e: TimeoutException => log.warning("Worker " + a + " is not responding")
        }
      }
    }
    returnValue
  }

  override def iterator = {
    workers.foreach { a =>
      a ! CommandRequest.iterator
    }
    Iterator.empty
  }
}
