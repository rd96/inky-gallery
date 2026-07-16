ALTER TABLE sessions DROP CONSTRAINT fk_sessions_user_id__id;

ALTER TABLE sessions ADD CONSTRAINT fk_sessions_user_id__id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT;