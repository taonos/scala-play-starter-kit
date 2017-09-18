create table account (
  id serial NOT NULL PRIMARY KEY,
  firstname VARCHAR NOT NULL,
  lastname VARCHAR NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER user_update_modified_at_on_update
  BEFORE UPDATE ON account
  FOR EACH ROW
  EXECUTE PROCEDURE moddatetime (updated_at);

CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON account
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();