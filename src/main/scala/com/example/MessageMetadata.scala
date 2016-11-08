package com.example

import java.util.Date

import akka.actor._
import com.example.MetaData.Entry

object MessageMetadataDriver extends CompletableApp(3) {
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

import MeataData._

case class MeataData(entries: List[Entry]) {
  def this() = this(List.empty[Entry])
  def this(entry: Entry) = this(List[List](entry))

  def including(entry: Entry): MeataData = {
    MeataData(entries :+ entry)
  }
}
