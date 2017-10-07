ALTER TABLE account
  ADD COLUMN credential_id uuid;

ALTER TABLE account
  ADD CONSTRAINT fk_account__credential FOREIGN KEY (credential_id)
  REFERENCES credential (id) MATCH SIMPLE
  ON UPDATE CASCADE
  ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_account__credential ON account
  IS 'This foreign key references password. If the user is registered via third party login, it is possible that the user does not have a password yet.';