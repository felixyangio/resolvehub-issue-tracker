-- V4: Seed demo users for production demo access
-- Password for all demo accounts is: password
-- BCrypt hash of "password" (cost 10)

INSERT INTO users (id, name, email, password_hash, role, enabled, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'Alice Chen',   'alice@tenant.io',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMSurdBrua', 'USER',    true, NOW(), NOW()),
  (gen_random_uuid(), 'Bob Torres',   'bob@property.io',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMSurdBrua', 'AGENT',   true, NOW(), NOW()),
  (gen_random_uuid(), 'Carol Perry',  'carol@property.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMSurdBrua', 'MANAGER', true, NOW(), NOW()),
  (gen_random_uuid(), 'David Kim',    'david@property.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMSurdBrua', 'AGENT',   true, NOW(), NOW()),
  (gen_random_uuid(), 'Admin User',   'admin@resolvehub.io','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMSurdBrua', 'ADMIN',  true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
