//package slick
//
//import java.net.URL
//import com.github.tminglei.slickpg._
//import slick.jdbc.JdbcCapabilities
//import slick.jdbc.PostgresProfile
//trait MyPostgresProfile extends ExPostgresProfile
//  with PgArraySupport
//  with PgDate2Support
//  with PgRangeSupport
//  with PgHStoreSupport
//  with PgCirceJsonSupport
//  with PgSearchSupport
//  with PgPostGISSupport
//  with PgNetSupport
//  with PgLTreeSupport {
//
//  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"
//
//  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
//  override protected def computeCapabilities =
//    super.computeCapabilities + JdbcCapabilities.insertOrUpdate
//
//  override val api = MyAPI
//
//  object MyAPI extends API with ArrayImplicits
//    with DateTimeImplicits
//    with JsonImplicits
//    with NetImplicits
//    with LTreeImplicits
//    with RangeImplicits
//    with HStoreImplicits
//    with SearchImplicits
//    with SearchAssistants {
//
//    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
//
////    implicit val playJsonArrayTypeMapper =
////      new AdvancedArrayJdbcType[JsValue](pgjson,
////        (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse(_))(s).orNull,
////        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
////      ).to(_.toList)
//
//    // Mapping between String and URL
//    implicit val strURLTypeMapper = MappedColumnType.base[URL, String](
//      url => url.toString,
//      str => new URL(str)
//    )
//  }
//}
//
//object MyPostgresProfile extends MyPostgresProfile
