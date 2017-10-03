create table auth_token (
  id UUID NOT NULL,
  account_id UUID NOT NULL,
  expiry TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  CONSTRAINT fk_auth_token__account FOREIGN KEY (account_id)
    REFERENCES account (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE RESTRICT
);

-- CREATE TRIGGER user_update_modified_at_on_update
--   BEFORE UPDATE ON auth_token
--   FOR EACH ROW
--   EXECUTE PROCEDURE moddatetime (updated_at);

CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON auth_token
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();