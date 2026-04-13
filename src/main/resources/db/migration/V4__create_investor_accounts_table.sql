CREATE SEQUENCE investor_accounts_seq
START WITH 1
INCREMENT BY 50
CACHE 10;

CREATE TABLE investor_accounts(
    id BIGINT DEFAULT NEXTVAL('investor_accounts_seq') PRIMARY KEY,
    email CITEXT UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    investor_application_id BIGINT UNIQUE NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_investor_accounts_investor_applications
        FOREIGN KEY(investor_application_id)
        REFERENCES investor_applications(id)
);

CREATE TRIGGER on_investor_accounts_update
    BEFORE UPDATE ON investor_accounts
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
