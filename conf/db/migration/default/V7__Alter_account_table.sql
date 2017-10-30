ALTER TABLE account DROP COLUMN provider_id;

ALTER TABLE account DROP COLUMN provider_key;

ALTER TABLE account DROP COLUMN salt;

ALTER TABLE account DROP COLUMN password;

ALTER TABLE account DROP COLUMN hasher;

ALTER TABLE account
  ADD CONSTRAINT account_username_check CHECK (username ~ '^[a-zA-Z0-9._-]{3,}$');

COMMENT ON CONSTRAINT account_username_check ON account
IS 'Username regex with following rules:
1. length >= 3
2. Valid characters: a-z, A-Z, 0-9, points, dashes and underscores.';

ALTER TABLE account
  ADD CONSTRAINT account_email_check CHECK (email ~ '^[a-zA-Z0-9\.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$');
