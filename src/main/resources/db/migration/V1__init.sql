CREATE SEQUENCE investor_applications_seq
START WITH 1
INCREMENT BY 50
CACHE 10;

CREATE EXTENSION IF NOT EXISTS citext;
CREATE TYPE INVESTOR_APPLICATION_STATUS as enum('PENDING','ACCEPTED','REJECTED');

CREATE TABLE investor_applications(
    id BIGINT DEFAULT NEXTVAL('investor_applications_seq') PRIMARY KEY ,
    firstname VARCHAR(64) NOT NULL ,
    lastname VARCHAR(64) NOT NULL ,
    email CITEXT UNIQUE NOT NULL,
    identity_document_key VARCHAR(64),
    address_proof_document_key VARCHAR(64),
    application_status INVESTOR_APPLICATION_STATUS NOT NULL DEFAULT 'PENDING',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.created_at != OLD.created_at THEN
        RAISE EXCEPTION 'created_at is immutable';
    END IF;
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER on_investor_applications_update
    BEFORE UPDATE ON investor_applications
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();