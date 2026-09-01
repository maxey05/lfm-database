INSERT INTO app_user (username, password_hash, full_name, role, enabled) VALUES
    ('admin', '$2b$10$cs1WTZFKTDyaANQhQgRML.Vk0Iyzdu.RIFGkQaMRDo1rCBeRikzsS', 'LFM Administrator', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;
