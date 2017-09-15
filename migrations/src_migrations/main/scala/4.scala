import mySlick.profile.MyPostgresProfile.api._
import com.liyaos.forklift.slick.SqlMigration

object M4 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(4)(List(
    sqlu"""CREATE TABLE "products" ("id" INTEGER NOT NULL PRIMARY KEY, "name" VARCHAR NOT NULL)"""
  ))
}
