INSERT INTO lendings (client_id, book_id, taken_at, returned_at, created_at, updated_at)
SELECT c.id, b.id, now() - (x.days || ' days')::interval, NULL, now(), now()
FROM (
    VALUES
        ('Ivan Petrov', '9780132350884', 2),
        ('Anna Sidorova', '9780134685991', 1),
        ('Pavel Smirnov', '9780201633610', 4),
        ('Sergey Ivanov', '9780321125217', 5),
        ('Maria Kuznetsova', '9780134757599', 6),
        ('Dmitry Orlov', '9780131177055', 7),
        ('Elena Morozova', '9780321349606', 3),
        ('Alexey Fedorov', '9781617294945', 9),
        ('Olga Volkova', '9781544869179', 8),
        ('Nikita Pavlov', '9789730228236', 10),
        ('Daria Mikhailova', '9783950307825', 11),
        ('Andrey Novikov', '9781492040347', 12),
        ('Polina Vasilyeva', '9781680502398', 13),
        ('Roman Sokolov', '9781492034025', 14),
        ('Ksenia Lebedeva', '9781617294549', 15),
        ('Viktor Kozlov', '9780596805821', 16),
        ('Yulia Zaitseva', '9781492078005', 17),
        ('Mikhail Egorov', '9780135957059', 18),
        ('Alina Nikolaeva', '9780321146533', 19),
        ('Kirill Stepanov', '9780321127426', 20)
) AS x(client_name, isbn, days)
JOIN clients c ON c.full_name = x.client_name
JOIN books b ON b.isbn = x.isbn
WHERE NOT EXISTS (
    SELECT 1
    FROM lendings l
    WHERE l.client_id = c.id
      AND l.book_id = b.id
      AND l.returned_at IS NULL
);

INSERT INTO lendings (client_id, book_id, taken_at, returned_at, created_at, updated_at)
SELECT c.id, b.id, now() - interval '30 days', now() - interval '20 days', now(), now()
FROM clients c
JOIN books b ON b.isbn = '9780321125217'
WHERE c.full_name = 'Sofia Andreeva'
  AND NOT EXISTS (
      SELECT 1
      FROM lendings l
      WHERE l.client_id = c.id
        AND l.book_id = b.id
        AND l.returned_at IS NOT NULL
  );
