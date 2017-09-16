import akka.actor.{Actor, ActorLogging, ActorSystem, Props, SupervisorStrategy}
import com.typesafe.config.ConfigFactory


class Manager extends Actor with ActorLogging with Serializable{

  case class Response(msg: String) extends Serializable

   val dict = Map("1" -> Response("星期一"), "2" -> Response("星期二"), "3" -> Response("星期三"), "4" -> Response("星期四"), "5" -> Response("星期五"), "6" -> Response("星期六"), "1" -> Response("星期日"))
//   val dict = Map("1" -> "星期一", "2" -> "星期二", "3" -> "星期三", "4" -> "星期四", "5" -> "星期五", "6" -> "星期六", "1" -> "星期日")

  def handle(msg: Any): Unit = {
    log.info("收到客户端消息，开始处理...")
    Thread.sleep(1000L)
    sender ! dict(msg.toString) //发送响应信息到请求者
  }

  override def receive: Receive = {
    case msg if "0123456789".contains(msg) => handle(msg)
    case msg => sender ! Response("sorry, I don't known.")
//    case msg => sender ! "sorry, I don't known."
  }
}

object RemoteServer extends App {
  val conf = ConfigFactory.parseResources("akka.conf").getConfig("RemoteServerActor")
  val server = ActorSystem("server", conf)
  val manager = server.actorOf(Props[Manager], "manager")
}


