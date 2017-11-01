CREATE OR REPLACE VIEW account_credential
WITH (security_barrier=true)
  AS
    SELECT a.id AS account_id,
      a.username,
      a.email,
      a.credential_id,
      c.hasher,
      c.hashed_password,
      c.salt,
      c.created_at,
      c.updated_at
    FROM account a
      LEFT JOIN credential c ON a.credential_id = c.id;