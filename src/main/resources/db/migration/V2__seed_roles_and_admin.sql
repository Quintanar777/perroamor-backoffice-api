INSERT INTO roles (name, description, is_active) VALUES
    ('ADMIN',    'Acceso total: catálogo, ventas, usuarios, eventos.', TRUE),
    ('MANAGER',  'Gestiona catálogo, eventos y supervisa ventas.',     TRUE),
    ('EMPLOYEE', 'Operador de POS — registra ventas en eventos.',      TRUE);

-- Admin inicial — password: admin123 (BCrypt cost 12). CAMBIAR EN PRODUCCIÓN.
INSERT INTO users (username, password_hash, email, full_name, role_id, is_active)
SELECT
    'admin',
    '$2a$12$8kl64xIOCmnYjGalh1gQNOUGSq7VGq89GJRylLQkj0exir8PLJUhi',
    'admin@perroamor.local',
    'Administrador',
    r.id,
    TRUE
FROM roles r
WHERE r.name = 'ADMIN';
