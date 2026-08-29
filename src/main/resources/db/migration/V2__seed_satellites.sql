INSERT INTO church_satellite (name, active) VALUES
    ('Main', true),
    ('Ortigas', true),
    ('Makati', true),
    ('Quezon City', true)
ON CONFLICT (name) DO NOTHING;