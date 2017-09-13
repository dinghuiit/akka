import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Kill, PoisonPill, Props}

object MyAkka {
  def main(args: Array[String]): Unit = {
    //1. 定义一个ActorSystem对象，用于管理所有actor
    val demo = ActorSystem("demo");
    //2. 定义各个actor角色，其中student包含一个teacher的引用，用于给teacher发消息
    val teacher = demo.actorOf(Props[Teacher],"teacher")
    val student = demo.actorOf(Props(new Student(teacher)), "student")
    val watcher = demo.actorOf(Props[MyDeadLetterActor], "watcher")
    //3. 将DeadLetter监听器注册到事件流中，监听DeadLetter发送的消息
    demo.eventStream.subscribe(watcher,classOf[MyDeadLetterActor])
    while (true) {
      val command = scala.io.StdIn.readLine("请输入要执行的命令：\n")
      student ! command
      Thread.sleep(1000l)
    }
//    Thread.sleep(1000L)
//    demo.terminate()
  }
}

class Student(teacher:ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("{} started", sender.path)
  override def postStop(): Unit = log.info("{} stoped", sender.path)

  override def receive: Receive = {
    case "stop" | "quit" => context.stop(self)
    case "kill" => log.info("I'm killing myself");self ! Kill
    case "poison" => log.info("I'm poisoning teacher...");teacher ! PoisonPill
    case msg : String => teacher ! msg
    case msg if msg.isInstanceOf[Message] => log.info("received message {}", msg) //为了接收teacher返回的消息
  }
}

class Teacher extends Actor with ActorLogging{
  /**
    * 1. 构造一个字典，用了比较曲折的方式，主要是为了学习一下zip和toMap方法
    * 还可以直接用Map（key1->value1,key2->value2）方式构造
    * 2. 此处方法属于构造器方法，对象初始化时执行
    */
  val boys = Array("萧峰","段誉","慕容复","虚竹","段正淳")
  val girls = Array("阿朱","王语嫣","阿紫","钟灵","梦姑")
  var cupple = boys.zip(girls).toMap

  /**
    * 重写这两个方法为了在Actor启动和停止时记录日志
    */
  override def preStart(): Unit = log.info("{} started", sender.path)
  override def postStop(): Unit = log.info("{} stoped", sender.path)

  /**
    * 重点是重写此方法，用于接收消息
    * @return
    */
  override def receive: Receive = {
    case boy if boys.contains(boy) => sender ! new Message(s"$boy's wife is ${cupple(boy.asInstanceOf[String])}")
    case boy => log.info("unknown people!")
  }
}
//定义一个类，用于teacher返回消息给student
class Message(msg: String)

//定义一个死亡消息监听，监听Deadletter发送的消息
class MyDeadLetterActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case letter:DeadLetter=>log.info(s"Actor[$letter] is dead")
  }
}