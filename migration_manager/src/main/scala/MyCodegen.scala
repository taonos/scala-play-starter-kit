import com.liyaos.forklift.slick.SlickCodegen
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.model.Model

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex

// override the default code generator here
trait MyCodegen extends SlickCodegen {
  // change directory of generated file options here
  // override val generatedDir = ...
  // override val container = ...
  // override val fileName = ...

  override val container: String = "Tables"

  val migrationTableName = "__migrations__"

  override def getTables(driver: JdbcProfile): DBIO[Model] =
    driver.createModel(Some(driver.defaultTables.map { s =>
      // run codegen for all tables except migration table
      s.filterNot { t =>
        t.name.name == migrationTableName
      }
    }))

  import slick.model.Model

  class MySlickSourceCodeGenerator(m: Model, version: Int) extends SlickSourceCodeGenerator(m, version) {

    /**
      * A workaround for the profile passed in has extra symbols at the end. For example,
      * "mySlick.profile.MyPostgresProfile$@1fbecd5". This function strips the unnecessary symbols.
      * @param profile Slick profile that is imported in the generated package (e.g. slick.jdbc.H2Profile)
      * @return Fixed profile string.
      */
    private def cleanProfile(profile: String): String =
      profile.substring(0, profile.indexOf("$"))

    // ensure to use our customized postgres driver at `import profile.simple._`
    override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String = {
      val cleanedProfile = cleanProfile(profile)

      s"""
        |package ${pkg}
        |// AUTO-GENERATED Slick data model
        |/** Stand-alone Slick data model for immediate use */
        |object ${container} extends {
        |  val profile = $cleanedProfile
        |} with ${container}
        |
        |/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
        |trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
        |  val profile: $cleanedProfile
        |  import profile.api._
        |  ${indent(code)}
        |}
        |
        |object Version{
        |  def version = $version
        |}
      """.stripMargin.trim
    }
  }


  override def getGenerator(m: Model, version: Int): SlickSourceCodeGenerator =
    new MySlickSourceCodeGenerator(m, version)
}
