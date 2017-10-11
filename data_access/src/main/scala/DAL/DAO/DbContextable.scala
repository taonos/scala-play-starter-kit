package DAL.DAO

import DAL.DbContext

trait DbContextable {

  val ctx: DbContext
}
