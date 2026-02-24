DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conrelid = 'public.item_variants'::regclass
      AND contype = 'f'
      AND conname = 'fk_item_variants_item_id'
  ) THEN
    ALTER TABLE public.item_variants
      ADD CONSTRAINT fk_item_variants_item_id
      FOREIGN KEY (item_id) REFERENCES public.items(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conrelid = 'public.sale_lines'::regclass
      AND contype = 'f'
      AND conname = 'fk_sale_lines_sale_id'
  ) THEN
    ALTER TABLE public.sale_lines
      ADD CONSTRAINT fk_sale_lines_sale_id
      FOREIGN KEY (sale_id) REFERENCES public.sales(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conrelid = 'public.sale_lines'::regclass
      AND contype = 'f'
      AND conname = 'fk_sale_lines_variant_id'
  ) THEN
    ALTER TABLE public.sale_lines
      ADD CONSTRAINT fk_sale_lines_variant_id
      FOREIGN KEY (variant_id) REFERENCES public.item_variants(id);
  END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_item_variants_sku ON public.item_variants (sku);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sales_reference ON public.sales (reference);
CREATE INDEX IF NOT EXISTS idx_item_variants_item_id ON public.item_variants (item_id);
CREATE INDEX IF NOT EXISTS idx_sale_lines_sale_id ON public.sale_lines (sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_lines_variant_id ON public.sale_lines (variant_id);
