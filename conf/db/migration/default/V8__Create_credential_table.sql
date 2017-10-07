CREATE TABLE credential
(
  id uuid NOT NULL,
  hasher character varying(30) NOT NULL,
  hashed_password character varying(255) NOT NULL,
  salt character varying(255),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (id)
);


CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON credential
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();

COMMENT ON TABLE credential
  IS 'Saves a user''s logins. A record is created when a user registers with his credentials. Third party logins are save in different tables.';