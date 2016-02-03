package edu.rosehulman.client

import akka.actor.ActorSystem
import akka.event.Logging
import edu.rosehulman.raft.RaftActor
import edu.rosehulman.raft.messages.RequestVoteRequest
import scala.collection.JavaConverters._

object Application {
  val system = ActorSystem("raft")
  val log = Logging.getLogger(system, this)

  def main(args: Array[String]) = {
    val ref = system.actorOf(RaftActor.props)
    val nodes = system.settings.config.getList("raft.cluster").unwrapped.asScala.map(_.toString)
    log.info("Created actor at " + ref.path)
    ref ! RequestVoteRequest(1, 1, 1, 1)
    /*

      term: Term,
  candidateId: CandidateId,
  lastLogIndex: Index,
  lastLogTerm: Index
     */
  }
}
