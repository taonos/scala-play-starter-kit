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

CREATE OR REPLACE FUNCTION insert_and_update_on_account_credential()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  NOT LEAKPROOF
AS $function$
BEGIN
  IF tg_op = 'UPDATE' THEN
    INSERT INTO credential(id, hasher, hashed_password, salt, created_at, updated_at)
      VALUES (NEW.credential_id, NEW.hasher, NEW.hashed_password, NEW.salt, NEW.created_at, NEW.updated_at)
      ON CONFLICT (id)
        DO UPDATE SET hasher = EXCLUDED.hasher, hashed_password = EXCLUDED.hashed_password, salt = EXCLUDED.salt,
          updated_at = EXCLUDED.updated_at;
    UPDATE account SET credential_id = NEW.credential_id, updated_at = NEW.updated_at WHERE id = OLD.account_id;
    RETURN NEW;
  ELSIF tg_op = 'DELETE' THEN
    UPDATE account SET credential_id = NULL, updated_at = NEW.updated_at WHERE id = OLD.account_id;
    DELETE FROM credential WHERE id = OLD.credential_id;
    RETURN 1;
  END IF;
END;
$function$;

COMMENT ON FUNCTION public.insert_and_update_on_account_credential()
  IS 'On update, upsert password info into credential table and update credential_id foreign key on account table. On delete, delete the row from credential table and credential_id foreign key to null on account table.';

CREATE TRIGGER on_update_and_delete_trigger
  INSTEAD OF UPDATE OR DELETE ON account_credential
  FOR EACH ROW
  EXECUTE PROCEDURE insert_and_update_on_account_credential();