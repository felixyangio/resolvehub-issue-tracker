-- V6: Fix demo user password hashes (V4 had an incorrect bcrypt hash)
-- Password for all demo accounts is: password

UPDATE users
SET password_hash = '$2b$10$hh4LXXB6wKYKyHQ05oZqNeVH6zbT8wdy4zw1jYMhA2c0PF4Nk8Th.'
WHERE email IN (
  'alice@tenant.io',
  'bob@property.io',
  'carol@property.io',
  'david@property.io',
  'admin@resolvehub.io'
);
