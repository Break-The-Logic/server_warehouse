WITH variant_seed AS (
  SELECT 2001::BIGINT AS id, 1001::BIGINT AS item_id, 'MIG-SEED-A-RED-42'::VARCHAR(100) AS sku, 'Seed Variant A Red 42'::VARCHAR(160) AS name, 129.90::NUMERIC(16, 2) AS price, 50::INTEGER AS stock_quantity, true::BOOLEAN AS active
  UNION ALL
  SELECT 2002::BIGINT, 1001::BIGINT, 'MIG-SEED-A-BLU-43'::VARCHAR(100), 'Seed Variant A Blue 43'::VARCHAR(160), 139.90::NUMERIC(16, 2), 20::INTEGER, true::BOOLEAN
)
INSERT INTO public.item_variants (id, item_id, sku, name, price, stock_quantity, active, created_at, updated_at)
SELECT id, item_id, sku, name, price, stock_quantity, active, NOW(), NOW()
FROM variant_seed
WHERE EXISTS (
  SELECT 1
  FROM public.items
  WHERE items.id = variant_seed.item_id
)
ON CONFLICT (id) DO UPDATE
SET
  item_id = EXCLUDED.item_id,
  sku = EXCLUDED.sku,
  name = EXCLUDED.name,
  price = EXCLUDED.price,
  stock_quantity = EXCLUDED.stock_quantity,
  active = EXCLUDED.active,
  updated_at = NOW();
