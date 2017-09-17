create function ignore_created_at_change() returns trigger language plpgsql as $$
begin

  new.created_at = old.created_at;

  return new;

end $$;