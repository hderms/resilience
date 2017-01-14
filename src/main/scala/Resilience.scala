package com.rtx
import akka.stream.scaladsl._
import akka.stream.{ Inlet, Outlet, Shape, Graph, FlowShape, ActorMaterializer }
import scala.collection._
import akka.NotUsed
import akka.actor.ActorSystem
import GraphDSL.Implicits._
import akka.stream.scaladsl.{ GraphDSL, Broadcast, Zip }
import akka.stream.FlowShape
import akka.routing._
import scala.concurrent.Future

object Resilience {

  implicit val system = ActorSystem()

  implicit val mat = ActorMaterializer()
  implicit val ec = mat.executionContext

  def toFutureLength(s: String): Future[Int] = {
    Future {throw new Exception("foo")}
  }

  def identity[T](x: T): T = x

  def test = {
    val errorFlow = Flow[(String, Int)]
      .recover { case x: Exception => x }.to(Sink.foreach(println _))

    val toHarden = Flow[String].map(toFutureLength _)
    harden(toHarden).alsoTo(errorFlow)
      .runWith(Source(List("foobarbaz", "f", "1234567")), Sink.foreach(println _))
  }

  def harden[A, B](asyncFlow: Flow[A, Future[B], Any]) = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    // prepare graph elements
    val broadcast = b.add(Broadcast[A](2))
    val zip = b.add(Zip[A, Future[B]]())

    // connect the graph
    broadcast.out(0).map(identity) ~> zip.in0
    broadcast.out(1).via(asyncFlow) ~> zip.in1

    // expose ports
    val oot = zip.out.map { x => x._2.map((x._1, _)) }.mapAsyncUnordered(10)(identity _).outlet
    FlowShape(broadcast.in, oot)
  })

}





object Main extends App {
  implicit val system= ActorSystem()
  implicit val mat= ActorMaterializer()
  implicit val ec= mat.executionContext
Resilience.test
}
