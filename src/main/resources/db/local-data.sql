INSERT INTO books (id, title, author, isbn, created_at, updated_at)
VALUES
    (1, 'Effective Java', 'Joshua Bloch', '9780134685991', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Clean Code', 'Robert C. Martin', '9780132350884', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Java Concurrency in Practice', 'Brian Goetz', '9780321349606', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE books ALTER COLUMN id RESTART WITH 4;
