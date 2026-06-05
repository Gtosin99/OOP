CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL REFERENCES roles(id),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_verifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verification_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE test_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    tat_hours INTEGER NOT NULL CHECK (tat_hours > 0),
    result_format VARCHAR(30) NOT NULL
);

CREATE TABLE test_requests (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES users(id),
    test_type_id INTEGER NOT NULL REFERENCES test_types(id),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_completion TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED'
);

CREATE TABLE samples (
    id SERIAL PRIMARY KEY,
    test_request_id INTEGER NOT NULL REFERENCES test_requests(id) ON DELETE CASCADE,
    current_status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sample_status_history (
    id SERIAL PRIMARY KEY,
    sample_id INTEGER NOT NULL REFERENCES samples(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    updated_by INTEGER NOT NULL REFERENCES users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE results (
    id SERIAL PRIMARY KEY,
    test_request_id INTEGER NOT NULL REFERENCES test_requests(id) ON DELETE CASCADE,
    file_path VARCHAR(255) NOT NULL,
    result_type VARCHAR(30) NOT NULL,
    validation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    uploaded_by INTEGER NOT NULL REFERENCES users(id),
    validated_by INTEGER REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP
);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (role_name)
VALUES ('ADMIN'), ('LAB_SCIENTIST'), ('CUSTOMER')
ON CONFLICT (role_name) DO NOTHING;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_test_requests_customer_id ON test_requests(customer_id);
CREATE INDEX idx_samples_test_request_id ON samples(test_request_id);
CREATE INDEX idx_results_test_request_id ON results(test_request_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
