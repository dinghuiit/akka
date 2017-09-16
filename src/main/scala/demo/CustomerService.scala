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
