class RaftActor() extends Actor {

  def receive = {
    case x: Int => sender() ! (x + 5)

  }
}
