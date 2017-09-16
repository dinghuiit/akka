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
