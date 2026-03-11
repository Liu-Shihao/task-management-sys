INSERT INTO `user` (name, email, password_hash)
SELECT 'Dev User', 'dev@finblock.local', 'c91cbbedf8c712e8e2b7517ddeca8fe4fde839ebd8339e0b2001363002b37712'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'dev@finblock.local');

