CREATE SEQUENCE user_id_seq
START WITH 1
INCREMENT BY 1;

CREATE SEQUENCE investor_application_id_seq
START WITH 1
INCREMENT BY 1;

CREATE TABLE users(
    user_id BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL
);


CREATE TABLE investor_applications(
    investor_application_id BIGINT PRIMARY KEY  DEFAULT nextval('investor_application_id_seq'),
    user_id BIGINT,
    identity_document_key TEXT,
    proof_of_address_document_key TEXT,
    selfie_document_key TEXT,
    CONSTRAINT fk_investor_applications_users FOREIGN KEY(user_id)
                                  REFERENCES users(user_id)
)