INSERT INTO public.items (id, name, description, active, created_at, updated_at)
VALUES
  (1001, 'Migration Seed Item A', 'Primary seeded item for immediate API testing', true, NOW(), NOW()),
  (1002, 'Migration Seed Item B', 'Secondary seeded item for delete and update endpoint testing', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  active = EXCLUDED.active,
  updated_at = NOW();
