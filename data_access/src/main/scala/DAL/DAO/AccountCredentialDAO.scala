package DAL.DAO

import DAL.DbContext
import javax.inject.{Inject, Singleton}
import DAL.table.AccountCredentialTable
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountCredentialDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountCredentialTable](_.accountId, _.createdAt)
}
