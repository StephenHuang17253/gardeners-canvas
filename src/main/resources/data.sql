INSERT INTO USER_TABLE (DATE_OF_BIRTH,VERIFIED,USER_ID,FIRST_NAME,LAST_NAME,PROFILE_PICTURE_FILENAME,EMAIL_ADDRESS,PASSWORD)
VALUES
('2001-01-01', TRUE, 1, 'Sheldon','Cooper',NULL,'sheldoncopper@email.com','{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1975-05-01', TRUE, 2, 'George', 'Cooper', '../../images/user_2_profile_picture.jpg', 'georgecoopersr@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1992-11-25', TRUE, 3, 'George', 'Cooper', '../../images/user_3_profile_picture.jpg', 'georgecooperjr@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1945-03-10', TRUE, 4, 'Meemaw', '', NULL, 'meemawcooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('2001-01-01', TRUE, 5, 'Missy', 'Cooper', NULL, 'missycooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1972-12-12', TRUE, 6, 'Mary', 'Cooper', NULL, 'marycooper@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('2001-04-15', TRUE, 7, 'Billy', 'Sparks', NULL, 'billysparks@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1960-08-20', TRUE, 8, 'Jeff', 'Difford', NULL, 'jeffdifford@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq'),
('1978-03-24', TRUE, 9, 'Stuart', 'Bloom', NULL, 'stuartbloom@email.com', '{bcrypt}$2a$10$NMK7UwogRwxjjMlOqHjF9.8XSdleK4qzjd6tCEksLP3Bp34SnWBoq');

INSERT INTO FRIENDSHIP_TABLE (STATUS,FRIEND_SHIP_ID,USER1_USER_ID,USER2_USER_ID)
VALUES
(0,1,1,5), -- pending request from Sheldon to Missy
(1,2,6,1), -- Sheldon and Mary friends
(0,3,9,1), -- pending request from Stuart to Sheldon
(2,4,7,1), -- declined request from Billy to Sheldon
(2,5,1,8); --  declined request from Sheldon to Jeff