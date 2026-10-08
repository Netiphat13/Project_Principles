CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(255),
    avatar_url VARCHAR(500),
    phone VARCHAR(50),
    bio TEXT,

    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    language VARCHAR(20),
    currency VARCHAR(10),
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_settings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    created_by BIGINT NOT NULL,
    restaurant_name VARCHAR(255),
    bill_date DATE,
    bill_time TIME,
    note TEXT,
    subtotal NUMERIC(12,2),
    discount NUMERIC(12,2),
    service_charge NUMERIC(12,2),
    vat NUMERIC(12,2),
    total_amount NUMERIC(12,2),
    status VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_bills_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE TABLE bill_members (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    user_id BIGINT,
    guest_name VARCHAR(255),
    guest_token VARCHAR(255) UNIQUE,
    joined_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_bill_members_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_bill_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE bill_items (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_bill_items_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id)
        ON DELETE CASCADE
);

CREATE TABLE item_assignments (
    id BIGSERIAL PRIMARY KEY,
    bill_item_id BIGINT NOT NULL,
    bill_member_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_item_assignments_item
        FOREIGN KEY (bill_item_id)
        REFERENCES bill_items(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_item_assignments_member
        FOREIGN KEY (bill_member_id)
        REFERENCES bill_members(id)
        ON DELETE CASCADE
);

CREATE TABLE split_configs (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL UNIQUE,
    split_method VARCHAR(50) NOT NULL,
    config_data JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_split_configs_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id)
        ON DELETE CASCADE
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    bill_member_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(50),
    payment_method VARCHAR(50),
    paid_at TIMESTAMP,

    CONSTRAINT fk_payments_member
        FOREIGN KEY (bill_member_id)
        REFERENCES bill_members(id)
        ON DELETE CASCADE
);

CREATE TABLE settlements (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    from_member_id BIGINT NOT NULL,
    to_member_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(50),
    settled_at TIMESTAMP,

    CONSTRAINT fk_settlements_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_settlements_from_member
        FOREIGN KEY (from_member_id)
        REFERENCES bill_members(id),

    CONSTRAINT fk_settlements_to_member
        FOREIGN KEY (to_member_id)
        REFERENCES bill_members(id)
);

CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    created_by BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_groups_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE TABLE group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50),
    joined_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_group_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(255),
    message TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);