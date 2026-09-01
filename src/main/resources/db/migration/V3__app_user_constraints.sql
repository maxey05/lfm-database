ALTER TABLE app_user
    ADD CONSTRAINT uq_app_user_username UNIQUE (username);

ALTER TABLE app_user
    ADD CONSTRAINT ck_app_user_role CHECK (role IN ('VIEWER', 'STAFF', 'ADMIN'));
