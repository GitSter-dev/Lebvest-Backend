CREATE SEQUENCE set_password_tokens_seq
START WITH 1
INCREMENT BY 50
CACHE 10;

CREATE TABLE set_password_tokens(
    id BIGINT DEFAULT NEXTVAL('set_password_tokens_seq') PRIMARY KEY,
    token TEXT UNIQUE NOT NULL,
    investor_application_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    CONSTRAINT fk_set_password_tokens_investor_applications
                                FOREIGN KEY(investor_application_id)
                                REFERENCES investor_applications(id)

);

CREATE FUNCTION on_update()
RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.created_at != OLD.CREATED_AT THEN
            RAISE EXCEPTION 'created_at is immutable';
        END IF;
    END

$$ LANGUAGE plpgsql;

CREATE TRIGGER on_update_trigger
BEFORE UPDATE ON set_password_tokens
FOR EACH ROW
EXECUTE FUNCTION on_update()


