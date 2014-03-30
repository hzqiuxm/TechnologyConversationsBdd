package controllers

import org.specs2.mutable.{After, Specification}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, FakeApplication}
import models.Composites
import play.api.libs.json.Json
import org.specs2.matcher.{PathMatchers, JsonMatchers}
import java.io.File

class CompositesControllerSpec extends Specification with JsonMatchers with PathMatchers {

  "GET /composites" should {

    "return OK" in {
      running(FakeApplication()) {
        val Some(result) = route(FakeRequest(GET, "/composites"))
        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
      }
    }

    "return same output as Composites#classesToJson" in {
      running(FakeApplication()) {
        val Some(result) = route(FakeRequest(GET, "/composites"))
        val composites = Composites("app/composites")
        contentAsJson(result) must equalTo(composites.classesToJson(composites.list()))
      }
    }

  }

  "GET /composites/*className" should {

    "return OK if className is correct" in {
      running(FakeApplication()) {
        val Some(result) = route(FakeRequest(GET, "/composites/composites.com.technologyconversations.bdd.steps.WebStepsComposites"))
        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
      }
    }

    "return BAD_REQUEST if className is incorrect" in {
      running(FakeApplication()) {
        val Some(result) = route(FakeRequest(GET, "/composites/non.existent.class.json"))
        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
      }
    }

    "return same output as Composites#classToJson" in {
      running(FakeApplication()) {
        val className = "composites.com.technologyconversations.bdd.steps.WebStepsComposites"
        val Some(result) = route(FakeRequest(GET, s"/composites/$className"))
        status(result) must equalTo(OK)
        val composites = Composites("app/composites")
        contentAsJson(result) must equalTo(composites.classToJson(className))
      }
    }

  }

  "PUT /composites routes" should {

    val url = "/composites"
    val fakeJsonHeaders = FakeHeaders(Seq("Content-type" -> Seq("application/json")))
    val packageName = "composites.com.technologyconversations.bdd.steps"
    val className = "TestComposites"
    val dirPath = "app" + File.separator + packageName.replace(".", File.separator)
    val fullPath = dirPath + File.separator + className + ".java"
    val jsonMap = Map(
      "package" -> Json.toJson(packageName),
      "class" -> Json.toJson(className),
      "composites" -> Json.toJson(List[String]())
    )

    "return BAD_REQUEST if body is NOT JSON" in {
      running(FakeApplication()) {
        val Some(result) = route(FakeRequest(PUT, url))
        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        contentAsString(result) must /("message" -> noJsonResultMessage)
      }
    }

    "return BAD_REQUEST if package is not present" in {
      running(FakeApplication()) {
        val json = Json.toJson(jsonMap.filterKeys(_ != "package"))
        val Some(result) = route(FakeRequest(PUT, url, fakeJsonHeaders, json))
        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        contentAsString(result) must /("message" -> noResultMessage("package"))
      }
    }

    "save file" in new AfterStoryControllerSpec(fullPath) {
      running(FakeApplication()) {
        val json = Json.toJson(jsonMap)
        val Some(result) = route(FakeRequest(PUT, url, fakeJsonHeaders, json))
        fullPath must beAnExistingPath
        fullPath must beAFilePath
      }
    }

  }

  class AfterStoryControllerSpec(path: String) extends After {

    override def after = {
      val file = new File(path)
      if (file.exists) {
        file.delete
      }
    }

  }

}
