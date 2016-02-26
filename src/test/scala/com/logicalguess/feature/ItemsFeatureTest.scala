package com.logicalguess.feature

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.logicalguess.ItemServer
import com.logicalguess.domain.Item
import com.twitter.finagle.http.Status._
import com.twitter.finatra.http.test.{EmbeddedHttpServer, HttpTest}
import com.twitter.inject.Mockito
import com.twitter.inject.server.{EmbeddedTwitterServer, FeatureTest}
import org.junit.runner.RunWith
import org.scalatest.{GivenWhenThen, BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.junit.JUnitRunner

/**
  * Created by logicalguess on 2/25/16.
  */

@RunWith(classOf[JUnitRunner])
class ItemsFeatureTest extends FeatureTest with Mockito with HttpTest with BeforeAndAfterEach with BeforeAndAfterAll with GivenWhenThen {

  val objectMapper = new ObjectMapper() with ScalaObjectMapper
  objectMapper.registerModule(DefaultScalaModule)

  override val server = new EmbeddedHttpServer(new ItemServer)

  "Post Controller" should {

    "return the items" in {

      Given("items exist in the database")

      When("the api receives a request for a list of all items")
      val response = server.httpGet(path = s"/api/items/list")

      Then("all items are returned")
      val content = response.getContentString
      val items = objectMapper.readValue[Seq[Item]](content)

      response.getStatusCode() shouldEqual 200
      items.length should be > 1
    }

    "create a new item" in {

      When("The api receives a POST request with a valid item")
      val response = server.httpPost(path = "/api/items/add",
        postBody =
          """
             {"title": "some item title", "desc": "some item desc"}
          """,
        andExpect = Ok)

      Then("the post is created and returned")
      response.getStatusCode() shouldBe 200
      val content = response.getContentString()
      val item = objectMapper.readValue[Item](content)
      item.title shouldBe "some item title"
    }
  }
}
