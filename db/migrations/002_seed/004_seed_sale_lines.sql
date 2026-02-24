WITH sale_line_seed AS (
  SELECT 4001::BIGINT AS id, 3001::BIGINT AS sale_id, 2001::BIGINT AS variant_id, 2::INTEGER AS quantity, 129.90::NUMERIC(16, 2) AS unit_price, 259.80::NUMERIC(18, 2) AS line_total
)
INSERT INTO public.sale_lines (id, sale_id, variant_id, quantity, unit_price, line_total)
SELECT id, sale_id, variant_id, quantity, unit_price, line_total
FROM sale_line_seed
WHERE EXISTS (
  SELECT 1
  FROM public.sales
  WHERE sales.id = sale_line_seed.sale_id
)
  AND EXISTS (
    SELECT 1
    FROM public.item_variants
    WHERE item_variants.id = sale_line_seed.variant_id
)
ON CONFLICT (id) DO UPDATE
SET
  sale_id = EXCLUDED.sale_id,
  variant_id = EXCLUDED.variant_id,
  quantity = EXCLUDED.quantity,
  unit_price = EXCLUDED.unit_price,
  line_total = EXCLUDED.line_total;

UPDATE public.item_variants
SET stock_quantity = 48,
    updated_at = NOW()
WHERE id = 2001;

UPDATE public.sales
SET total_amount = 259.80,
    updated_at = NOW()
WHERE id = 3001;
