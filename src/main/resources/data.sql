INSERT INTO USER_TABLE (DATE_OF_BIRTH,VERIFIED,USER_ID,FIRST_NAME,LAST_NAME,PROFILE_PICTURE_FILENAME,EMAIL_ADDRESS,PASSWORD)
VALUES
('2001-01-01', TRUE, 101, 'Sheldon','Cooper',NULL,'sheldoncooper@email.com','{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1975-05-01', TRUE, 102, 'George', 'Cooper', '../../images/user_2_profile_picture.jpg', 'georgecoopersr@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1992-11-25', TRUE, 103, 'George', 'Cooper', '../../images/user_3_profile_picture.jpg', 'georgecooperjr@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1945-03-10', TRUE, 104, 'Meemaw', '', NULL, 'meemawcooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('2001-01-01', TRUE, 105, 'Missy', 'Cooper', NULL, 'missycooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1972-12-12', TRUE, 106, 'Mary', 'Cooper', NULL, 'marycooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('2001-04-15', TRUE, 107, 'Billy', 'Sparks', NULL, 'billysparks@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1960-08-20', TRUE, 108, 'Jeff', 'Difford', NULL, 'jeffdifford@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1978-03-24', TRUE, 109, 'Stuart', 'Bloom', NULL, 'stuartbloom@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq');

INSERT INTO FRIENDSHIP_TABLE (STATUS,FRIEND_SHIP_ID,USER1_USER_ID,USER2_USER_ID)
VALUES
(0,101,101,105), -- pending request from Sheldon to Missy
(1,102,106,101), -- Sheldon and Mary friends
(0,103,109,101), -- pending request from Stuart to Sheldon
(2,104,107,101), -- declined request from Billy to Sheldon
(2,105,101,108); --  declined request from Sheldon to Jeff