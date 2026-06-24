CREATE TABLE IF NOT EXISTS lendings
(
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT    NOT NULL,
    book_id     BIGINT    NOT NULL,
    taken_at    TIMESTAMP NOT NULL,
    returned_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT fk_lendings_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_lendings_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT chk_lendings_returned_after_taken CHECK (returned_at IS NULL OR returned_at >= taken_at)
);

CREATE INDEX IF NOT EXISTS idx_lendings_client_id ON lendings (client_id);
CREATE INDEX IF NOT EXISTS idx_lendings_book_id ON lendings (book_id);
CREATE INDEX IF NOT EXISTS idx_lendings_active_taken_at ON lendings (taken_at DESC) WHERE returned_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_lendings_active_client_book ON lendings (client_id, book_id) WHERE returned_at IS NULL;
