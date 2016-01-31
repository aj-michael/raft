package edu.rosehulman.raft

import akka.actor.{Actor, FSM}
import edu.rosehulman.raft.messages.{RequestVoteResponse, RequestVoteRequest, AppendEntriesRequest}

class RaftActor() extends Actor with FSM[State, Data] {

  startWith(Follower, new NonleaderData())

  when(Follower) {
    case Event(AppendEntriesRequest, d: NonleaderData) =>
      stay using d
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), d: NonleaderData) =>
      val vote = term >= d.currentTerm && d.votedFor.isEmpty
      sender() ! RequestVoteResponse(d.currentTerm, vote)
      stay using d
  }

  when(Candidate) {
    case Event(RequestVoteResponse(term, vote), d: NonleaderData) =>
      stay using d
  }

  when(Leader) {
    case Event(RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm), d: LeaderData) =>
      stay using d
  }

  whenUnhandled {
    case Event(e, s) =>
      log.warning("Received unhandled request {}", e)
      stay
  }

  initialize()
}
