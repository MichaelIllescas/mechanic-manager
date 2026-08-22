CREATE TABLE users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    status        VARCHAR(32)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                      ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('SYSTEM_ADMIN', 'USER')),
    CONSTRAINT ck_users_status CHECK (status IN ('ENABLED', 'DISABLED'))
);

CREATE TABLE workshops
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    name       VARCHAR(120) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    email      VARCHAR(255) NULL,
    address    VARCHAR(255) NULL,
    notes      TEXT         NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                   ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_workshops PRIMARY KEY (id),
    CONSTRAINT uq_workshops_user UNIQUE (user_id),
    CONSTRAINT fk_workshops_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
);