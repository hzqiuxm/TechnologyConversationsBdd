package models

import models.db.BddDb
import models.jbehave.JBehaveStory
import models.file.{BddFile, FileTraitStory}
import java.io.File
import play.api.libs.json.JsValue
import util.Imports._


// TODO Move methods from extended classes to objects set as constructor arguments
// TODO Remove dir and path
class Story(val dir: String = "",
            val path: String = "",
            val bddFile: Option[BddFile] = Option.empty,
            val bddDb: Option[BddDb] = Option.empty)
  extends JBehaveStory with FileTraitStory {

  def saveStory(file: File, json: JsValue, overwrite: Boolean): Boolean = {
    var dbSuccess = true
    var fileSuccess = true
    if (bddDb.isDefined && featureIsEnabled("mongoDb")) {
      val storyPath = (json \ "path").as[String]
      dbSuccess = bddDb.get.upsertStory(storyPath, json)
    }
    if (bddFile.isDefined) {
      fileSuccess = bddFile.get.saveFile(file, toText(json), overwrite)
    }
    dbSuccess && fileSuccess
  }

}

// TODO Remove dir and path
object Story {

  def apply(dir: String = "",
            path: String = "",
            bddFile: Option[BddFile] = Option.empty,
            bddDb: Option[BddDb] = Option.empty): Story = {
    new Story(dir, path, bddFile, bddDb)
  }

}
