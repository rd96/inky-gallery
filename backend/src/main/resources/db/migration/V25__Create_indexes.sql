CREATE INDEX canvases_created_by ON canvases (created_by);
CREATE INDEX sessions_user_id ON sessions (user_id);
CREATE INDEX connections_recipient_id ON connections (recipient_id);
CREATE INDEX account_tokens_user_id ON account_tokens (user_id);