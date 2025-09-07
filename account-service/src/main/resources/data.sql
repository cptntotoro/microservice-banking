-- Заполняем таблицу ролей
INSERT INTO roles (name, description)
VALUES ('ROLE_USER', 'Обычный пользователь'),
       ('ROLE_ADMIN', 'Администратор')
ON CONFLICT (name) DO NOTHING;

-- Вставка основных валют
-- INSERT INTO currencies (id, code, name)
-- VALUES (gen_random_uuid(), 'USD', 'US Dollar'),
--        (gen_random_uuid(), 'EUR', 'Euro'),
--        (gen_random_uuid(), 'RUB', 'Russian Ruble'),
--        (gen_random_uuid(), 'GBP', 'British Pound'),
--        (gen_random_uuid(), 'JPY', 'Japanese Yen');
--
-- -- Вставка начальных курсов валют
-- INSERT INTO exchange_rates (id, base_currency_id, target_currency_id, buy_rate, sell_rate, effective_date)
-- SELECT gen_random_uuid(),
--        (SELECT id FROM currencies WHERE code = 'USD'),
--        (SELECT id FROM currencies WHERE code = 'RUB'),
--        75.50,
--        76.20,
--        CURDATE()
-- UNION ALL
-- SELECT gen_random_uuid(),
--        (SELECT id FROM currencies WHERE code = 'EUR'),
--        (SELECT id FROM currencies WHERE code = 'RUB'),
--        82.30,
--        83.00,
--        CURDATE()
-- UNION ALL
-- SELECT gen_random_uuid(),
--        (SELECT id FROM currencies WHERE code = 'USD'),
--        (SELECT id FROM currencies WHERE code = 'EUR'),
--        0.85,
--        0.86,
--        CURDATE();