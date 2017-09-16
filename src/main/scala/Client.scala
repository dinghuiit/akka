import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class Customer extends Actor with ActorLogging with Serializable{
  val path = "akka.tcp://server@127.0.0.1:1000/user/manager"
  val remoteServer = context.actorSelection(path)

  override def receive: Receive = {
    case msg: String => remoteServer ! msg //发送消息到远程客服
    case msg => log.info(msg.asInstanceOf) //打印客服回复的消息
  }
}

object RemoteClient extends App {
  val conf = ConfigFactory.parseResources("akka.conf").getConfig("RemoteClientActor")
  val client = ActorSystem("client", conf)
  val customer = client.actorOf(Props[Customer], "customer")
  while (true) {
    val question = readLine("请提问:")
    customer ! question
  }
}