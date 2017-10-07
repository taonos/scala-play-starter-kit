//package DAL.table
//
//import DAL.DAO.{PK, TableWithPK}
//import io.getquill.Embedded
//
///**
//  *
//  * @param providerId The ID of the provider.
//  * @param providerKey A unique key which identifies a user on this provider (userID, email, ...).
//  */
//final case class ExternalLoginPK(providerId: String, providerKey: String) extends PK with Embedded
//
///**
//  *
//  * @param pk
//  * @param accountId
//  */
//final case class ExternalLoginTable(pk: ExternalLoginPK, accountId: AccountId) extends TableWithPK[ExternalLoginPK] with Timestamped {
//
//}