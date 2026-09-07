CREATE TABLE users (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    app_user_id BIGINT,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    admin_role VARCHAR(50) NOT NULL,
    created_at DATE NOT NULL,
    last_login_at TIMESTAMP,
    CONSTRAINT fk_admin_user FOREIGN KEY (app_user_id) REFERENCES users(id)
);

CREATE TABLE coaches (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    app_user_id BIGINT,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    CONSTRAINT fk_coach_user FOREIGN KEY (app_user_id) REFERENCES users(id)
);

CREATE TABLE members (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    app_user_id BIGINT,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    section VARCHAR(255) NOT NULL,
    expiry_date DATE,
    pass_validity BOOLEAN,
    coach_id BIGINT,
    CONSTRAINT fk_member_user FOREIGN KEY (app_user_id) REFERENCES users(id),
    CONSTRAINT fk_member_coach FOREIGN KEY (coach_id) REFERENCES coaches(id)
)

