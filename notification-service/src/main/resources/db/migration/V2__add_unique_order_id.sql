DROP INDEX idx_notifications_order_id ON notifications;

ALTER TABLE notifications
    ADD CONSTRAINT uk_notifications_order_id UNIQUE (order_id);