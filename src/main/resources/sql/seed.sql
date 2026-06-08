INSERT INTO users(full_name, email, password_hash, role, email_verified, force_password_change)
VALUES
('Super Admin', 'admin@sante.com', '$2a$10$MtFZr2o9R5NQlR1zEzAwy.Sa6G6Kdb4waLFTdlF1YbfjJM7X5v3li', 'SUPER_ADMIN', TRUE, FALSE),
('Lab Attendant', 'lab@sante.com', '$2a$10$qed3mp9JFEA/wJT3KpUROu2m41EHePAdXSZx.PQwyIB5/lkUjp.OC', 'LAB_ATTENDANT', TRUE, FALSE),
('Demo Customer', 'customer@sante.com', '$2a$10$ypH328J3jgAqdHB.D1RP5uyCx.SC.V5uOHEMot7ibeOLFzY7ihR8q', 'CUSTOMER', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO test_catalog(name, category, price, tat_hours, result_format, active)
VALUES
('Complete Blood Count', 'Blood', 7500.00, 24, 'PDF', TRUE),
('Lipid Profile', 'Blood', 9800.00, 48, 'PDF', TRUE),
('MRI Brain Scan', 'Imaging', 120000.00, 72, 'IMAGE', TRUE),
('Biopsy Histology', 'Biopsy', 48000.00, 96, 'PDF', TRUE)
ON CONFLICT DO NOTHING;
