import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class HelloActor extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println(message);
    }
    public static void main(String[] args) {
        ActorSystem demo = ActorSystem.create("demo");
        ActorRef actor1 = demo.actorOf(Props.create(HelloActor.class),"helloActor");
        actor1.tell("nihao",actor1);
    }
}


