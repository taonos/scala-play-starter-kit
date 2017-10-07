//package DAL.DAO
//
//import javax.inject.{Inject, Singleton}
//
//import DAL.DbContext
//import DAL.table.{ExternalLoginPK, ExternalLoginTable}
//import monix.eval.Task
//
//import scala.concurrent.{ExecutionContext, Future}
//
//@Singleton
//class ExternalLoginDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext)
//    extends DAOCrudWithPk[Future, ExternalLoginTable, ExternalLoginPK] {
//  import ctx._
//
//  private implicit val updateExclusion =
//    updateMeta[ExternalLoginTable](_.pk, _.createdAt)
//
//  override val table = quote(querySchema[ExternalLoginTable]("external_login"))
//
//  override def findAll: Future[Seq[ExternalLoginTable]] = ???
//
//  override def findByPk(pk: ExternalLoginPK): Future[Option[ExternalLoginTable]] =
//    run(
//      table.filter(v =>
//        v.pk.providerId == lift(pk.providerId) &&
//          v.pk.providerKey == lift(pk.providerKey))
//    )
//      .map(_.headOption)
//
//  override def insert(row: ExternalLoginTable): Future[ExternalLoginTable] = ???
//
//  override def insertBatch(rows: Seq[ExternalLoginTable]): Future[Long] = ???
//
//  override def update(row: ExternalLoginTable): Future[ExternalLoginTable] = ???
//
//  override def updateBatch(rows: Seq[ExternalLoginTable]): Future[Long] = ???
//
//  override def deleteByPk(pk: ExternalLoginPK): Future[Unit] = ???
//}
