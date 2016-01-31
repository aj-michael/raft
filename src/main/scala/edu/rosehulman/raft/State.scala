package edu.rosehulman.raft

sealed trait State
case object Follower extends State
case object Candidate extends State
case object Leader extends State