CREATE SEQUENCE admin_users_seq
    START WITH 1
    INCREMENT BY 50
    CACHE 10;

CREATE TABLE admin_users(
    id BIGINT DEFAULT NEXTVAL('admin_users_seq') PRIMARY KEY,
    email CITEXT UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'ADMIN',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER on_admin_users_update
    BEFORE UPDATE ON admin_users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
