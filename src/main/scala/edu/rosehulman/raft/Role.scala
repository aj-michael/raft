package edu.rosehulman.raft

sealed trait Role

object Role {
  case object Follower extends Role
  case object Candidate extends Role
  case object Leader extends Role
}