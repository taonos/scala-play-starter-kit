create table account (
  id UUID NOT NULL,
  username VARCHAR(100) NOT NULL,
  email VARCHAR NOT NULL,
  firstname VARCHAR NOT NULL,
  lastname VARCHAR NOT NULL,
  hasher VARCHAR NOT NULL,
  password VARCHAR NOT NULL,
  salt VARCHAR,
  provider_id VARCHAR NOT NULL,
  provider_key VARCHAR NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (id),
  CONSTRAINT unique_username UNIQUE (username),
  CONSTRAINT unique_email UNIQUE (email)
);

-- CREATE TRIGGER user_update_modified_at_on_update
--   BEFORE UPDATE ON account
--   FOR EACH ROW
--   EXECUTE PROCEDURE moddatetime (updated_at);

CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON account
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();