DROP TABLE IF EXISTS user_profile;

CREATE TABLE user_profile
(
    id         INT PRIMARY KEY,
    name       VARCHAR(64) NOT NULL,
    level      INT         NOT NULL,
    experience INT         NOT NULL,
    energy     INT         NOT NULL,
    rating     INT         NOT NULL,
    money      INT         NOT NULL,
    backpack   VARCHAR     NOT NULL,
    inventory  VARCHAR     NOT NULL,
    friends    VARCHAR     NOT NULL
);

DROP TABLE IF EXISTS uid_profile;

CREATE TABLE uid_profile
(
    uid        VARCHAR(64) NOT NULL PRIMARY KEY,
    profile_id INT         NOT NULL,
    UNIQUE (uid, profile_id)
);

DROP SEQUENCE IF EXISTS user_profile_sequence;

CREATE SEQUENCE user_profile_sequence
    START WITH 1
    INCREMENT BY 1;
