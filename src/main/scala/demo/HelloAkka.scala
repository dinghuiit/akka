package demo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object HelloAkka extends App{
  val helloService = ActorSystem("HelloService")
  val actor = helloService.actorOf(Props[HelloActor],"hello")
  actor ! "hello"
  actor ! "world"
  actor ! "stop"
  helloService.shutdown()
}
class HelloActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case "hello" => log.info("hello => 你好！")
    case msg if "world"==msg => log.info(s"$msg => 世界")
    case "quit"|"stop" => log.info("我要停机了！");context stop(self)
  }
}
