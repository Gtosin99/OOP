INSERT INTO users(full_name, email, password_hash, role, email_verified, force_password_change)
VALUES
('Demo Customer', 'customer@sante.com', '$2a$10$h4f8s6mWDqFTx8hm3jTJ1ON9f/GwWY7caQCFJXhLxD.7YVBYA5f2C', 'CUSTOMER', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO test_catalog(name, category, price, tat_hours, result_format, active)
VALUES
('Complete Blood Count', 'Blood', 7500.00, 24, 'PDF', TRUE),
('Lipid Profile', 'Blood', 9800.00, 48, 'PDF', TRUE),
('MRI Brain Scan', 'Imaging', 120000.00, 72, 'IMAGE', TRUE),
('Biopsy Histology', 'Biopsy', 48000.00, 96, 'PDF', TRUE)
ON CONFLICT DO NOTHING;
