
[toc]
## 一、Akka简单介绍
> Akka基于Actor模型，提供了一个用于构建可扩展的（Scalable）、弹性的（Resilient）、快速响应的（Responsive）应用程序的平台。

Actor是Akka中最核心的概念，它是一个封装了状态和行为的对象，Actor之间可以通过交换消息的方式进行通信，每个Actor都有自己的收件箱（Mailbox）。
通过Actor能够简化锁及线程管理，可以非常容易地开发出正确地并发程序和并行系统，Actor具有如下特性：
+ 提供了一种高级抽象，能够简化在并发（Concurrency）/并行（Parallelism）应用场景下的编程开发
+ 提供了异步非阻塞的、高性能的事件驱动编程模型
+ 超级轻量级事件处理（每GB堆内存几百万Actor）

## 二、Akka简单使用
### 1. 从创建一个scala项目说起
1. 在D盘新建一个项目目录，比如说叫AkkaDemo。
2. 进入AkkaDemo目录，新建一个build.gradle文件，并在文件中输入一下内容:

```
apply plugin: 'idea'
apply plugin: 'scala'

task "createDirs" << {
    sourceSets*.scala.srcDirs*.each { it.mkdirs() }
    sourceSets*.resources.srcDirs*.each { it.mkdirs() }
}

repositories{
    mavenCentral()
    mavenLocal()
}

dependencies{
    compile "org.scala-lang:scala-library:2.10.4"
    compile "org.scala-lang:scala-compiler:2.10.4"
    compile "org.scala-lang:scala-reflect:2.10.4"
    compile "com.typesafe.akka:akka-actor_2.11:2.4.4"
    testCompile "junit:junit:4.11"
}

task run(type: JavaExec, dependsOn: classes) {
    main = 'Main'
    classpath sourceSets.main.runtimeClasspath
    classpath configurations.runtime
}

```
3. 执行`gradle cDirs`命令，创建起项目骨架。
4. 使用Idea导入gradle项目。File”->"Import Project"选择打开build.gradle即可。

### 2. 第一个Akka应用
使用Akka框架进行应用开发，基本上遵循以下步骤即可：
1. 编写一个Actor类，继承特质akka.actor.Actor，同时可以编入一些其他的特质，如ActorLogging，用于记录日志。
2. 实现Actor的receive方法，receive方法中定义一系列的case语句，基于标准Scala的模式匹配方法，来实现每一种消息的处理逻辑。
3. 编写程序入口，在入口程序中,创建一个顶层的ActorSystem。
4. 创建一个actor，可以使用ActorSystem或context的ActorOf方法创建，也可以使用context.actorSelection方法通过actor的名称从上下文中查找。
5. 向actor发送消息, ! 代表发送。


#### a. 定义一个Actor
+ 定义一个Actor类，继承Actor，编入ActorLogging特质（相当于实现一个接口)
+ 重写Actor类的Receive方法，用于接收消息。
+ 使用scala的模式匹配功能，根据不同消息做不同逻辑处理

```
class HelloActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case "hello" => log.info("hello => 你好！")
    case msg if "world"==msg => log.info(s"$msg => 世界")
    case "quit"|"stop" => log.info("我要停机了！");context stop(self)
  }
}
```
#### b. 客户端调用，向actor发送消息

以下为程序入口,其中actor为HelloActor实例的一个引用，通过！向actor发送消息；通过terminate方法关闭HelloService。

※※注意！※※
- ActorRef 类型的对象是不可变的，并且是可序列化的，可以在网络中进行传输，作为远程对象使用，具体的操作还是在本地Actor中进行。
- ActorRef在创建时可以不指定名称，即actorOf(Props[Class])。但是如果指定名称的话，需要保证在父级actor下名称是唯一的。
- actor名称不能是以“$”开头的字符串
- actor可以有带参构造，但是如果使用带参构造则不能使用Props[Class]创建，应使用Props(new HelloActor("",...))方式实例化。
- Actor的unhandled方法对 receive 方法中未匹配成功的消息进行处理，默认情况有两种处理方式：当未处理消息类型是 akka.actor.Terminated 时，抛出 akka.actor.DeathPactException；当其它未处理消息时，向akka.event.EventStream 发送 akka.actor.UnhandledMessage 类型消息

```
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
object HelloAkka extends App{
  val helloService = ActorSystem("HelloService")
  val actor = helloService.actorOf(Props[HelloActor],"hello")
  actor ! "hello"
  actor ! "world"
  actor ! "stop"
  helloService terminate
}
```
#### c. Actor的生命周期
- PreStart 方法只在第一次创建时被调用，来初始化 actor 实体。
- PostStop 方法一定在收件箱停止后才运行，用于关闭资源。
- 在重启的同时，收件箱不会被影响，可以继续接收消息。
- 消息发给已经停止的 actor 会被转发到系统的 deadLetters Actor。
- Actor 的构造函数在第一次创建和每次重启时被调用，来初始化 acto

#### d.Actor编程模型的层次结构
在Akka中，一个ActorSystem是一个重量级的结构，他需要分配多个线程，所以在实际应用中，按照逻辑划分的每个应用对应一个ActorSystem实例。
ActorSystem的Top-Level层次结构，与Actor关联起来，称为Actor路径（Actor Path），不同的路径代表了不同的监督范围（Supervision Scope）。下面是ActorSystem的监督范围：
+ “/”路径：通过根路径可以搜索到所有的Actor
+ “/user”路径：用户创建的Top-Level Actor在该路径下面，通过调用ActorSystem.actorOf来实现Actor的创建
+ “/system”路径：系统创建的Top-Level Actor在该路径下面
+ “/deadLetters”路径：消息被发送到已经终止，或者不存在的Actor，这些Actor都在该路径下面
+ “/temp”路径：被系统临时创建的Actor在该路径下面
+ “/remote”路径：改路径下存在的Actor，它们的Supervisor都是远程Actor的引用

### 3. akka的容错机制
一个ActorSystem是具有分层结构（Hierarchical Structure）的：一个Actor能够管理（Oversee）某个特定的函数，他可能希望将一个task分解为更小的多个子task，这样它就需要创建多个子Actor（Child Actors），并监督这些子Actor处理任务的进度等详细情况，实际上这个Actor创建了一个Supervisor来监督管理子Actor执行拆分后的多个子task，如果一个子Actor执行子task失败，那么就要向Supervisor发送一个消息说明处理子task失败。需要知道的是，一个Actor能且仅能有一个Supervisor，就是创建它的那个Actor。基于被监控任务的性质和失败的性质，一个Supervisor可以选择执行如下操作选择：
+ 重新开始（Resume）一个子Actor，保持它内部的状态
+ 重启一个子Actor，清除它内部的状态
+ 终止一个子Actor
+ 扩大失败的影响，从而使这个子Actor失败

### 4. akka的远程调用
下面是一个用户到餐厅点餐的案例，服务员属于服务端，通过加载customer.conf配置，将服务发布在1000端口上。客户端Actor通过服务端暴露服务链接，从上下文中选择一个远端waiter。然后在自身的receive方法中根据需要向服务端发送消息，同时在receive方法中接收waiter返回的消息。
```
  val waiterServiceUrl = "akka.tcp://WaiterService@127.0.0.1:1000/user/waiter"
  val waiter = context.actorSelection(path = waiterServiceUrl)
```
#### 客户端应用入口
```
package demo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * 客户端Actor，通过akka协议从上下文中查找服务端actor
  */
class Customer extends Actor with ActorLogging{
  val waiterServiceUrl = "akka.tcp://WaiterService@127.0.0.1:1000/user/waiter"
  val waiter = context.actorSelection(path = waiterServiceUrl)

  override def preStart(): Unit = log.info("customer is in!")
  override def postStop(): Unit = log.info("翔里有毒，我挂了！")

  override def receive = {
    case DishesOrder(name) => log.info(s"我点的美味[$name]上来了，开吃...")
    case SoupOrder(name) => log.info(s"我点的鲜汤[$name]上来了，开喝...")
    case FoodOrder(name) => log.info(s"我点的主食[$name]上来了，开吃...")
    case DrinkOrder(name) => log.info(s"我点的饮料[$name]上来了，开喝...")
    case FruitOrder(name) => log.info(s"我点的水果[$name]上来了，吃不了了打包...")
    case menu:Menu => waiter ! menu
    case other => waiter ! other
  }
}

/**
  * 客户端应用入口
  */
object CustomerService extends App{
  val customerService = ActorSystem("CustomerService",ConfigFactory.parseResources("customer.conf"))
  val customer = customerService.actorOf(Props[Customer],"customer")
  customerService.log.info("customer started!")
  customer ! Dishes(name=readLine("请输入您要点的菜：")); Thread sleep 1000L
  customer ! Soup(name=readLine("请输入您要点的汤：")); Thread sleep 1000L
  customer ! Food(name=readLine("请输入您要点的主食：")); Thread sleep 1000L
  customer ! Drink(name=readLine("请输入您要点的饮料：")); Thread sleep 1000L
  customer ! Fruit(name=readLine("请输入您要点的水果：")); Thread sleep 1000L
  customer ! readLine("您需要点别的吗：")
  customerService.shutdown()
}
```

#### 服务端入口
```
package demo

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import com.typesafe.config.ConfigFactory

class Waiter extends Actor with ActorLogging{
  override def preStart(): Unit = log.info("waiter is in!")
  override def postStop(): Unit = log.info("waiter is off!")
  override def receive = {
    case Dishes(name) => log.info(s"您点的[$name]菜品已下单."); sender ! DishesOrder(name)
    case Soup(name) => log.info(s"您点的[$name]汤已下单."); sender ! SoupOrder(name)
    case Food(name) => log.info(s"您点的主食[$name]已下单."); sender ! FoodOrder(name)
    case Drink(name) => log.info(s"您点的饮料[$name]已下单."); sender ! DrinkOrder(name)
    case Fruit(name) => log.info(s"您点的水果[$name]已下单."); sender ! FruitOrder(name)
    case other => log.info("您点的这个真没有,免费送你一盆翔。。。"); sender ! PoisonPill
  }
}

/**
  * 服务端
  */
object WaiterService extends App{
  val waiterService = ActorSystem("WaiterService",ConfigFactory.parseResources("waiter.conf"))
  val waiter = waiterService.actorOf(Props[Waiter],"waiter")
  waiterService.log.info("waiterService started!")
}

```
#### pojo类
```
package demo
trait Menu
final case class Dishes(name:String) extends Menu
final case class Soup(name:String) extends Menu
final case class Food(name:String) extends Menu
final case class Drink(name:String) extends Menu
final case class Fruit(name:String) extends Menu
trait Order
final case class DishesOrder(name:String) extends Order
final case class SoupOrder(name:String) extends Order
final case class FoodOrder(name:String) extends Order
final case class DrinkOrder(name:String) extends Order
final case class FruitOrder(name:String) extends Order

```
#### 客户端配置文件
```
akka {
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = 2000
    }
  }
}
```
#### 服务端配置文件
```
akka {
  actor {
    provider = "akka.remote.RemoteActorRefProvider"
  }
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = 1000
    }
  }
}
```
## 三、Spark2.0为什么放弃Akka
1. 很多Spark用户也使用Akka，但是由于Akka不同版本之间无法互相通信，这就要求用户必须使用跟Spark完全一样的Akka版本，导致用户无法升级Akka。
2. Spark的Akka配置是针对Spark自身来调优的，可能跟用户自己代码中的Akka配置冲突。
3. Spark用的Akka特性很少，这部分特性很容易自己实现。同时，这部分代码量相比Akka来说少很多，debug比较容易。如果遇到什么bug，也可以自己马上fix，不需要等Akka上游发布新版本。而且，Spark升级Akka本身又因为第一点会强制要求用户升级他们使用的Akka，对于某些用户来说是不现实的。

## 四、Akka适用场景
Akka适用场景非常广泛，这里根据一些已有的使用案例来总结一下，Akka能够在哪些应用场景下投入生产环境：

+ 事务处理（Transaction Processing）
在线游戏系统、金融/银行系统、交易系统、投注系统、社交媒体系统、电信服务系统。

+ 后端服务（Service Backend）
任何行业的任何类型的应用都可以使用，比如提供REST、SOAP等风格的服务，类似于一个服务总线，Akka支持纵向&横向扩展，以及容错/高可用（HA）的特性。

+ 并行计算（Concurrency/Parallelism）
任何具有并发/并行计算需求的行业，基于JVM的应用都可以使用，如使用编程语言Scala、Java、Groovy、JRuby开发。

+ 仿真
Master/Slave架构风格的计算系统、计算网格系统、MapReduce系统。

+ 通信Hub（Communications Hub）
电信系统、Web媒体系统、手机媒体系统。

+ 复杂事件流处理（Complex Event Stream Processing）
Akka本身提供的Actor就适合处理基于事件驱动的应用，所以可以更加容易处理具有复杂事件流的应用。
