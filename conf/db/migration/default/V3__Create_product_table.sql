CREATE TABLE product (
  id UUID NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- CREATE TRIGGER product_update_modified_at_on_update
--   BEFORE UPDATE ON product
--   FOR EACH ROW
--   EXECUTE PROCEDURE moddatetime (updated_at);

CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON product
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();