package io.ldivad.blog.example

import com.typesafe.config.ConfigFactory
import io.ldivad.blog.example.Configuration._
import org.apache.kafka.clients.admin.Admin
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

/**
 * Created by loicmdivad.
 */
object Main extends App {

  val config = ConfigFactory
    .parseResources("application.conf")
    .resolve()
    .getConfig("kafka-config")

  val logger = LoggerFactory
    .getLogger(getClass)

  val admin = Admin
    .create(config.toMap.asJava)

  logger info "Starting my random kafka client application"
  logger info "Listing the available topics: "

  admin.listTopics().names().get().asScala.foreach{ name =>
    logger info s"• 📨 The topic: '$name' has been found"
  }

  for (i <- 0 to 20) {
    print(s"\rDoing some actions ...${if (i % 2 == 0) "⏳" else "⌛️"}")
    Thread.sleep(500)
  }

  println()
  logger info "Stopping the application."
}
