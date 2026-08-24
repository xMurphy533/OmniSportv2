INSERT INTO admins (name, surname, email, password, role, is_active, created_at)
VALUES ('Nikodem', 'Kaczmarczyk', 'admin@omnisport.pl', 'superTajneHaslo', 'SUPER_ADMIN', true, CURRENT_DATE);

INSERT INTO coaches (name, surname, age, specialization)
VALUES ('Przemysław', 'Zbiciak', 42, 'MMA');

INSERT INTO members (name, surname, age, section, is_pass_valid ,expiry_date, coach_id)
VALUES ('Nikodem', 'Kaczmarczyk', 21, 'Kickboxing', true,CURRENT_DATE,1);