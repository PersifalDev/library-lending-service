CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS books
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    author     VARCHAR(255) NOT NULL,
    isbn       VARCHAR(32)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT chk_books_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT chk_books_author_not_blank CHECK (length(trim(author)) > 0),
    CONSTRAINT chk_books_isbn_not_blank CHECK (length(trim(isbn)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_books_title_trgm ON books USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_books_author_trgm ON books USING gin (lower(author) gin_trgm_ops);
