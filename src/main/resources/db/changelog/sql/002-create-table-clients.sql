CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS clients
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255) NOT NULL,
    birth_date DATE         NOT NULL,
    client_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_clients_login UNIQUE (login),
    CONSTRAINT chk_clients_login_not_blank CHECK (length(trim(login)) > 0),
    CONSTRAINT chk_clients_full_name_not_blank CHECK (length(trim(full_name)) > 0),
    CONSTRAINT chk_clients_birth_date_in_past CHECK (birth_date < CURRENT_DATE),
    CONSTRAINT chk_clients_role CHECK (client_role IN ('ADMIN', 'USER'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_clients_login ON clients (login);
CREATE INDEX IF NOT EXISTS idx_clients_role ON clients (client_role);
CREATE INDEX IF NOT EXISTS idx_clients_full_name_trgm ON clients USING gin (lower(full_name) gin_trgm_ops);
