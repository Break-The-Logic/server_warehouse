INSERT INTO public.sales (id, reference, total_amount, created_at, updated_at)
VALUES
  (3001, 'MIG-SEED-SALE-3001', 259.80, NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET
  reference = EXCLUDED.reference,
  total_amount = EXCLUDED.total_amount,
  updated_at = NOW();
