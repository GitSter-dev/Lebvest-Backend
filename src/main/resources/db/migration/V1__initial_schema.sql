CREATE SEQUENCE investor_signup_application_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;

CREATE TABLE investor_signup_applications (
    id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    country_of_residence VARCHAR(255) NOT NULL,
    identity_document_key VARCHAR(255),
    proof_of_residence_document_key VARCHAR(255),
    CONSTRAINT investor_signup_applications_pkey PRIMARY KEY (id),
    CONSTRAINT investor_signup_applications_email_key UNIQUE (email)
);

ALTER SEQUENCE investor_signup_application_id_seq OWNED BY investor_signup_applications.id;

CREATE TABLE source_of_funds_documents_keys (
    investor_signup_application_id BIGINT NOT NULL,
    source_of_funds_documents_keys VARCHAR(255),
    CONSTRAINT fk_sof_keys_application FOREIGN KEY (investor_signup_application_id)
        REFERENCES investor_signup_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_sof_keys_application_id ON source_of_funds_documents_keys (investor_signup_application_id);
