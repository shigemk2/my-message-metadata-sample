package com.example

import java.util.{Date, Random}

import akka.actor.Actor.Receive
import akka.actor._
import com.example.MetaData.Entry

object MessageMetadataDriver extends CompletableApp(3) {
  import MetaData._
  val processor3 = system.actorOf(Props(classOf[Processor], None), "processor3")
  val processor2 = system.actorOf(Props(classOf[Processor], Some(processor3)), "processor2")
  val processor1 = system.actorOf(Props(classOf[Processor], Some(processor2)), "processor1")

  val entry = Entry(
    Who("driver"),
    What("Started"),
    Where(this.getClass.getSimpleName, "driver"),
    new Date(),
    Why("Running processors")
  )

  processor1 ! SomeMessage("Data...", entry.asMetadata)

  awaitCompletion

  println("Completed.")
}

object MetaData {
  def apply() = new MetaData()

  case class Who(name: String)
  case class What(happend: String)
  case class Where(actorType: String, actorName: String)
  case class Why(explanation: String)
  case class Entry(who: Who, what: What, where: Where, when: Date, why: Why) {
    def this(who: Who, what: What, where: Where, why: Why) =
      this(who, what, where, new Date(), why)
    def this(who: String, what: String, actorType: String, actorName: String, why: String) =
      this(Who(who), What(what), Where(actorType, actorName), new Date(), Why(why))
    def asMetadata = (new MetaData(List[Entry](this)))
  }
}

case class MetaData(entries: List[Entry]) {
  def this() = this(List.empty[Entry])
  def this(entry: Entry) = this(List[Entry](entry))

  def including(entry: Entry): MetaData = {
    MetaData(entries :+ entry)
  }
}

case class SomeMessage(payload: String, metadata: MetaData = new MetaData()) {
  def including(entry: Entry): SomeMessage= {
    SomeMessage(payload, metadata.including(entry))
  }
}

class Processor(next: Option[ActorRef]) extends Actor {
  import MetaData._

  val random = new Random()

  override def receive: Receive = {
    case message: SomeMessage =>
      report(message)

      val nextMessage = message.including(entry)

      if (next.isDefined) {
        next.get ! nextMessage
      } else {
        report(nextMessage, "complete")
      }

      MessageMetadataDriver.completedStep()
  }

  def because = s"Because: ${random.nextInt(10)}"

  def entry =
    Entry(Who(user),
      What(wasProcessed),
      Where(this.getClass.getSimpleName, self.path.name),
      new Date(),
      Why(because))

  def report(message: SomeMessage, heading: String = "received") =
    println(s"${self.path.name} $heading: $message")

  def user = s"user${random.nextInt(100)}"

  def wasProcessed = s"Processed: ${random.nextInt(5)}"
}