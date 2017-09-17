CREATE TABLE "ownership"
(
  user_id integer NOT NULL,
  product_id integer NOT NULL,
  "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, product_id),
  CONSTRAINT fk_ownership__Users FOREIGN KEY (user_id)
  REFERENCES "user" (id) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE RESTRICT,
  CONSTRAINT fk_ownership__Products FOREIGN KEY (product_id)
  REFERENCES "product" (id) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE RESTRICT
);

CREATE TRIGGER ownership_update_modified_at_on_update
  BEFORE UPDATE ON "ownership"
  FOR EACH ROW
  EXECUTE PROCEDURE moddatetime (updated_at);

CREATE TRIGGER user_ignore_created_at_on_update
  BEFORE UPDATE ON "ownership"
  FOR EACH ROW
  EXECUTE PROCEDURE ignore_created_at_change();