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
