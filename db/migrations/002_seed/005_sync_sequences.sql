SELECT setval(
  pg_get_serial_sequence('public.items', 'id'),
  GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.items), 1),
  true
);

SELECT setval(
  pg_get_serial_sequence('public.item_variants', 'id'),
  GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.item_variants), 1),
  true
);

SELECT setval(
  pg_get_serial_sequence('public.sales', 'id'),
  GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.sales), 1),
  true
);

SELECT setval(
  pg_get_serial_sequence('public.sale_lines', 'id'),
  GREATEST((SELECT COALESCE(MAX(id), 1) FROM public.sale_lines), 1),
  true
);
