import mySlick.profile.MyPostgresProfile.api._
import com.liyaos.forklift.slick.DBIOMigration
import datamodel.v4.schema.Tables._

object M5 {
  MyMigrations.migrations = MyMigrations.migrations :+ DBIOMigration(5)(
    DBIO.seq(Products ++= Seq(
      ProductsRow(1, "Laptop"),
      ProductsRow(2, "Cellphone"),
      ProductsRow(3, "Pen")
    )))
}
