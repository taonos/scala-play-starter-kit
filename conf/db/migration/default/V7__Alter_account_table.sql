ALTER TABLE account DROP COLUMN provider_id;

ALTER TABLE account DROP COLUMN provider_key;

ALTER TABLE account
  ALTER COLUMN email TYPE character varying (100);

ALTER TABLE account
  ALTER COLUMN firstname TYPE character varying (30);

ALTER TABLE account
  ALTER COLUMN lastname TYPE character varying (30);

ALTER TABLE account DROP COLUMN salt;

ALTER TABLE account DROP COLUMN password;

ALTER TABLE account DROP COLUMN hasher;