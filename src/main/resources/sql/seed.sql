INSERT INTO users(full_name, email, password_hash, role, email_verified, force_password_change)
VALUES
('Super Admin', 'admin@sante.com', '$2a$10$6uBlaHXZOSR6oHxsolnXJeqcCBiaJkiYrHzN0LJS.gvOi/2u.jsUK', 'SUPER_ADMIN', TRUE, FALSE),
('Lab Attendant', 'lab@sante.com', '$2a$10$1ZJme22E8HJo9AAmYVUFcuC1vEuLXSwqZJLchzzrn1GGpthg8JMPW', 'LAB_ATTENDANT', TRUE, FALSE),
('Demo Customer', 'customer@sante.com', '$2a$10$Llymtqm0OBg56FVeb1mODOZjZeQ4STB9LuoDxYcRQKP7X3jzIa7y2', 'CUSTOMER', TRUE, FALSE)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    email_verified = EXCLUDED.email_verified,
    force_password_change = EXCLUDED.force_password_change;

INSERT INTO test_catalog(name, category, price, tat_hours, result_format, active)
VALUES
('Complete Blood Count', 'Blood', 7500.00, 24, 'PDF', TRUE),
('Lipid Profile', 'Blood', 9800.00, 48, 'PDF', TRUE),
('MRI Brain Scan', 'Imaging', 120000.00, 72, 'IMAGE', TRUE),
('Biopsy Histology', 'Biopsy', 48000.00, 96, 'PDF', TRUE)
ON CONFLICT DO NOTHING;
