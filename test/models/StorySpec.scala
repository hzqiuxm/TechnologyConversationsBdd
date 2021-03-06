package models

import models.db.BddDb
import models.file.BddFile
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import scala.Predef._
import play.api.libs.json.{JsValue, Json}
import org.jbehave.core.model.{Narrative, Lifecycle}
import models.jbehave.JBehaveStoryMock
import org.specs2.mock._
import java.io.File
import org.mockito.Mockito.{doReturn, reset}

class StorySpec extends Specification with Mockito with JsonMatchers {

  val storyName = "my_fancy"
  val storyDirPath = "path/to/"
  val storyPath = s"$storyDirPath$storyName.story"
  val storyJson = Json.parse(storyJsonString)

  "Story#rootCollection" should {

    val storyName = "my.story"
    val rootCollection = Story().rootCollection(storyName, "path/to/my.story", "")

    "have empty name when path is an empty string" in {
      rootCollection must havePair("name" -> Json.toJson(storyName))
    }

    "have empty description when path is an empty string" in {
      rootCollection must havePair("description" -> Json.toJson(""))
    }

    "have empty meta when path is an empty string" in {
      rootCollection must havePair("meta" -> Json.arr())
    }

    "have empty givenStories when path is an empty string" in {
      rootCollection  must havePair("givenStories" -> Json.arr())
    }

    "have empty scenarios when path is an empty string" in {
      rootCollection  must havePair("scenarios" -> Json.arr())
    }

    "have lifecycle with empty before and after when path is an empty string" in {
      rootCollection  must havePair("lifecycle" -> Json.toJson(JBehaveStoryMock.lifecycleCollection(new Lifecycle())))
    }

    "have narrative with empty inOrderTo, asA and iWantTo when path is an empty string" in {
      val narrative = new Narrative("", "", "")
      rootCollection  must havePair("narrative" -> Json.toJson(JBehaveStoryMock.narrativeCollection(narrative)))
    }

  }

  "Story#saveStory" should {

    val overwrite = true

    // TODO Remove
    "have upsertStory disabled by feature toggles" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = Story(bddDb = Option(bddDb))
      story.saveStory(file, storyJson, overwrite)
      there was no(bddDb).upsertStory(any[JsValue])
    }

    "call upsertStory" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      story.saveStory(file, storyJson, overwrite)
      there was one(bddDb).upsertStory(storyJson)
    }

    "NOT call bddDb when empty" in {
      val bddDbOption = mock[Option[BddDb]]
      Story(bddDb = bddDbOption)
      there was no(bddDbOption).get
    }

    "should return false when upsertStory is false" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      bddDb.upsertStory(any[JsValue]) returns false
      story.saveStory(file, storyJson, overwrite) must beFalse
    }

    "call saveFile" in {
      val file = mock[File]
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      story.saveStory(file, storyJson, overwrite)
      there was one(bddFile).saveFile(file, story.toText(storyJson), overwrite = true)
    }

    "NOT call bddFile when empty" in {
      val bddFile = mock[Option[BddFile]]
      Story(bddFile = bddFile)
      there was no(bddFile).get
    }

    "should return false when saveFile is false" in {
      val file = mock[File]
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      bddFile.saveFile(file, story.toText(storyJson), overwrite) returns false
      story.saveStory(file, storyJson, overwrite) must beFalse
    }

    "should return true when upsertStory and saveFile are true" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val bddFile = mock[BddFile]
      val story = Story(bddDb = Option(bddDb), bddFile = Option(bddFile))
      bddDb.upsertStory(any[JsValue]) returns true
      bddFile.saveFile(file, story.toText(storyJson), overwrite) returns true
      story.saveStory(file, storyJson, overwrite) must beTrue
    }

  }

  "Story#deleteStory" should {

    // TODO Remove
    "have BddDb#removeStory disabled by feature toggles" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = Story(bddDb = Option(bddDb))
      story.removeStory(file, storyPath)
      there was no(bddDb).removeStory(storyPath)
    }

    "call BddFile#deleteFile" in {
      val file = mock[File]
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      story.removeStory(file, storyPath)
      there was one(bddFile).deleteFile(file)
    }

    "NOT call BddFile#deleteFile when option is empty" in {
      val file = mock[File]
      val bddFileOption = mock[Option[BddFile]]
      val story = Story(bddFile = bddFileOption)
      story.removeStory(file, storyPath)
      there was no(bddFileOption).get
    }

    "return false when file was NOT deleted" in {
      val file = mock[File]
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      bddFile.deleteFile(file) returns false
      story.removeStory(file, storyPath) must beFalse
    }

    "call BddDb#removeStory" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      story.removeStory(file, storyPath)
      there was one (bddDb).removeStory(storyPath)
    }

    "NOT call BddDb#removeStory when option is empty" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val bddDbOption = mock[Option[BddDb]]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      story.removeStory(file, storyPath)
      there was no(bddDbOption).get
    }

    "return false when story was NOT removed from the DB" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      bddDb.removeStory(storyPath) returns false
      story.removeStory(file, storyPath) must beFalse
    }

    "return true when file was deleted and removed from the DB" in {
      val file = mock[File]
      val bddDb = mock[BddDb]
      val bddFile = mock[BddFile]
      val story = new Story(bddFile = Option(bddFile), bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      bddDb.removeStory(storyPath) returns true
      bddFile.deleteFile(file) returns true
      story.removeStory(file, storyPath) must beTrue
    }

  }

  "Story#findStory" should {

    val file = mock[File]

    // TODO Remove
    "have BddDb#findStory disabled by feature toggles" in {
      val bddDb = mock[BddDb]
      val story = Story(bddDb = Option(bddDb))
      story.findStory(file, storyPath)
      there was no(bddDb).findStory(storyPath)
    }

    "not call BddDb#findStory when option is empty" in {
      val bddDbOption = mock[Option[BddDb]]
      val story = new Story(bddDb = bddDbOption) {
        override val mongoDbIsEnabled = true
      }
      story.findStory(file, storyPath)
      there was no(bddDbOption).get
    }

    "return JSON from DB when BddDb is defined" in {
      val bddDb = mock[BddDb]
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      bddDb.findStory(storyPath) returns Option(storyJson)
      story.findStory(file, storyPath) must beEqualTo(Option(storyJson))
    }

    "return JSON from file when BddDb is empty" in {
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      file.getName returns s"$storyName.story"
      bddFile.fileToString(file) returns Option(storyString)
      story.findStory(file, storyPath).get must beEqualTo(storyJson)
    }

    "return empty when both BddDb and BddFile are empty" in {
      val story = Story()
      val emptyStory = Option(story.storyToJson("", storyPath, ""))
      story.findStory(file, storyPath) must beEqualTo(emptyStory)
    }

  }

  "Story#findStoryFromFile" should {

    val file = mock[File]

    "return JSON from file" in {
      val bddFile = mock[BddFile]
      val story = Story(bddFile = Option(bddFile))
      file.getName returns s"$storyName.story"
      bddFile.fileToString(file) returns Option(storyString)
      story.findStoryFromFile(file, storyPath).get must beEqualTo(storyJson)
    }

    "return empty option when bddFile is not defined" in {
      val story = Story()
      story.findStoryFromFile(file, storyPath) must beEqualTo(Option.empty)
    }

  }

  "Story#findStoryPaths" should {

    val dir1Name = "myDir1"
    val dir2Name = "myDir2"
    val story1Name = "myStory1"
    val story2Name = "myStory2"
    val dir = mock[File]
    val bddFile = mock[BddFile]
    val bddDb = mock[BddDb]
    val storiesPath = "path/to/some/dir"
    bddFile.listDirs(dir) returns Seq(dir1Name, dir2Name)
    bddFile.listFiles(dir) returns Seq(s"$story1Name.story", s"$story2Name.story")
    bddDb.findStoryDirPaths(storiesPath) returns Seq(dir1Name, dir2Name)
    bddDb.findStoryNames(storiesPath) returns Seq(story1Name, story2Name)

    // TODO Remove
    "have BddDb#findStoryDirPaths disabled by feature toggles" in {
      val bddDb = mock[BddDb]
      val story = Story(bddDb = Option(bddDb))
      story.findStoryPaths(dir, storiesPath)
      there was no(bddDb).findStoryDirPaths(any[String])
    }

    "return JSON with the list of directories from DB" in {
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      val json = story.findStoryPaths(dir, storiesPath).get.toString()
      json must /("dirs") */("name" -> dir1Name)
      json must /("dirs") */("name" -> dir2Name)
    }

    "return JSON with the list of stories from DB" in {
      val story = new Story(bddDb = Option(bddDb)) {
        override val mongoDbIsEnabled = true
      }
      val json = story.findStoryPaths(dir, storiesPath).get.toString()
      json must /("stories") */("name" -> story1Name)
      json must /("stories") */("name" -> story2Name)
    }

    "return JSON with the list of directories from FS" in {
      val story = Story(bddFile = Option(bddFile))
      val json = story.findStoryPaths(dir, storiesPath).get.toString()
      json must /("dirs") */("name" -> dir1Name)
      json must /("dirs") */("name" -> dir2Name)
    }

    "return JSON with the list of story files from FS" in {
      val story = Story(bddFile = Option(bddFile))
      val json = story.findStoryPaths(dir, storiesPath).get.toString()
      json must /("stories") */("name" -> story1Name)
      json must /("stories") */("name" -> story2Name)
    }

  }

  "Story#storiesFromFileToMongoDb" should {

    val dirPath = "path/to/stories"
    val dir = mock[File]
    val bddFile = mock[BddFile]
    val bddDb = mock[BddDb]
    val path1 = "path/to/my.story"
    val path2 = "path/to/another.story"
    bddFile.listFiles(any[File], any[String], any[Boolean], any[Option[String]]) returns List(path1, path2)

    "get the recursive list of story files" in {
      val story = spy(Story(bddFile = Option(bddFile), bddDb = Option(bddDb)))
      doReturn(Option(mock[JsValue])).when(story).findStoryFromFile(any[File], any[String])
      story.storiesFromFileToMongoDb(dirPath)
      there was one(bddFile).listFiles(dir, recursive = true, extension = Option(".story"))
    }

    "return false when BddFile is NOT defined" in {
      val story = Story(bddDb = Option(bddDb))
      story.storiesFromFileToMongoDb(dirPath) must beFalse
    }

    "return false when BddDb is NOT defined" in {
      val story = Story(bddFile = Option(bddFile))
      story.storiesFromFileToMongoDb(dirPath) must beFalse
    }

    "get story for each file" in {
      val story = spy(Story(bddFile = Option(bddFile), bddDb = Option(bddDb)))
      doReturn(Option(mock[JsValue])).when(story).findStoryFromFile(any[File], any[String])
      bddFile.listFiles(dir, recursive = true, extension = Option(".story")) returns List("a.story", "b.story")
      story.storiesFromFileToMongoDb(dirPath)
      there were two(story).findStoryFromFile(any[File], any[String])
    }

    "call findStoryFromFile correct parameters" in {
      val story = spy(Story(bddFile = Option(bddFile), bddDb = Option(bddDb)))
      doReturn(Option(mock[JsValue])).when(story).findStoryFromFile(any[File], any[String])
      bddFile.listFiles(dir, recursive = true, extension = Option(".story")) returns List("a.story", "b.story")
      story.storiesFromFileToMongoDb(dirPath)
      there was one(story).findStoryFromFile(new File(s"$dirPath/$path1"), path1)
    }

    "call upsertStory for each JSON" in {
      val story = spy(Story(bddFile = Option(bddFile), bddDb = Option(bddDb)))
      doReturn(Option(mock[JsValue])).when(story).findStoryFromFile(any[File], any[String])
      val storyJson1 = mock[JsValue]
      val storyJson2 = mock[JsValue]
      story.findStoryFromFile(any[File], any[String]) returns Option(storyJson1) thenReturns Option(storyJson2)
      bddFile.listFiles(dir, recursive = true, extension = Option(".story")) returns List("a.story", "b.story")
      story.storiesFromFileToMongoDb(dirPath)
      there was one(bddDb).upsertStory(storyJson1)
      there was one(bddDb).upsertStory(storyJson2)
    }

    "return true when operation was successful" in {
      val story = spy(Story(bddFile = Option(bddFile), bddDb = Option(bddDb)))
      doReturn(Option(mock[JsValue])).when(story).findStoryFromFile(any[File], any[String])
      story.storiesFromFileToMongoDb(dirPath) must beTrue
    }

  }

  "Story#storiesFromMongoDbToFiles" >> {

    val storiesPath = "path/to/stories"
    val bddFile = mock[BddFile]
    val bddDb = mock[BddDb]
    val storyPath1 = s"$storiesPath/story1.story"
    val storyPath2 = s"$storiesPath/story2.story"
    val storyJson1 = Json.parse(s"""{"path": "$storyPath1"}""")
    val storyJson2 = Json.parse(s"""{"path": "$storyPath2"}""")
    val storyAsText = "THIS IS STORY IN TEXT FORMAT"
    val story = new Story(bddFile = Option(bddFile), bddDb = Option(bddDb)) {
      override def toText(json: JsValue) = storyAsText
    }
    bddDb.findStories() returns Seq(storyJson1, storyJson2)

    "return false when BddFile is not defined" >> {
      val story = Story(bddDb = Option(bddDb))
      story.storiesFromMongoDbToFiles(storiesPath) must beFalse
    }

    "return false when BddDb is not defined" >> {
      val story = Story(bddFile = Option(bddFile))
      story.storiesFromMongoDbToFiles(storiesPath) must beFalse
    }

    "retrieve stories from the DB" >> {
      reset(bddDb)
      bddDb.findStories() returns Seq(storyJson1, storyJson2)
      story.storiesFromMongoDbToFiles(storiesPath)
      there was one(bddDb).findStories()
    }

    "call saveFile for each story" >> {
      reset(bddFile)
      story.storiesFromMongoDbToFiles(storiesPath)
      there were two(bddFile).saveFile(any[File], any[String], any[Boolean])
    }

    "call saveFile with correct params" >> {
      reset(bddFile)
      story.storiesFromMongoDbToFiles(storiesPath)
      there was one(bddFile).saveFile(
        new File(s"$storiesPath/$storyPath1"),
        storyAsText,
        overwrite = true
      )
    }

  }

  val storyString =
    """
This is description of this story

Meta:
@integration
@product dashboard

Narrative:
In order to communicate effectively to the business some functionality
As a development team
I want to use Behaviour-Driven Development

GivenStories: story.story

Lifecycle:

Before:
Given a step that is executed before each scenario

After:
Given a step that is executed after each scenario

Scenario: A scenario is a collection of executable steps of different type

Meta:
@live
@product shopping cart

Given step represents a precondition to an event

Examples:
|precondition|be-captured|
|abc|be captured|
|xyz|not be captured|
""".stripMargin
  lazy val storyJsonString =
    """
{
  "dirPath": "path/to/",
  "path": "path/to/my_fancy.story",
  "name": "my_fancy",
  "description": "This is description of this story",
  "meta": [ { "element": "integration" }, { "element": "product dashboard" } ],
  "narrative":
  {
    "inOrderTo": "communicate effectively to the business some functionality",
    "asA": "development team",
    "iWantTo": "use Behaviour-Driven Development"
  },
  "givenStories": [ { "story": "story.story" } ],
  "lifecycle":
  {
    "before": [ { "step": "Given a step that is executed before each scenario" } ],
    "after": [ { "step": "Given a step that is executed after each scenario" } ]
  },
  "scenarios":
  [
    {
      "title": "A scenario is a collection of executable steps of different type",
      "meta": [ { "element": "live" }, { "element": "product shopping cart" } ],
      "steps": [ { "step": "Given step represents a precondition to an event" } ],
      "examplesTable": "|precondition|be-captured|\n|abc|be captured|\n|xyz|not be captured|"
    }
  ]
}""".stripMargin

}
