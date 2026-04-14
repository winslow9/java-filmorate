-- Словари
CREATE TABLE IF NOT EXISTS DICT_MPA (
                                        id INTEGER PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS DICT_FILMGENRES (
                                               id INTEGER PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS DICT_FRIENDSHIPTYPE (
                                                   id INTEGER PRIMARY KEY,
                                                   name VARCHAR(255) NOT NULL
);

-- Основные таблицы
CREATE TABLE IF NOT EXISTS USERS (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     login VARCHAR(255) NOT NULL,
                                     name VARCHAR(255),
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     birthday DATE,
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS FILMS (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
                                     description VARCHAR(1000),
                                     release_date DATE,
                                     duration INTEGER CHECK (duration > 0),
                                     rate_id INTEGER,
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (rate_id) REFERENCES DICT_MPA(id) ON DELETE RESTRICT
);

-- Связующие таблицы
CREATE TABLE IF NOT EXISTS LINK_FILMGENRE (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              film_id BIGINT NOT NULL,
                                              genre_id INTEGER NOT NULL,
                                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (film_id) REFERENCES FILMS(id) ON DELETE CASCADE,
                                              FOREIGN KEY (genre_id) REFERENCES DICT_FILMGENRES(id) ON DELETE RESTRICT,
                                              UNIQUE (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS LINK_FILMLIKES (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              film_id BIGINT NOT NULL,
                                              user_id BIGINT NOT NULL,
                                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (film_id) REFERENCES FILMS(id) ON DELETE CASCADE,
                                              FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
                                              UNIQUE (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS FRIENDSHIPS (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           friend_id BIGINT NOT NULL,
                                           friendship_type_id INTEGER,
                                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
                                           FOREIGN KEY (friend_id) REFERENCES USERS(id) ON DELETE CASCADE,
                                           FOREIGN KEY (friendship_type_id) REFERENCES DICT_FRIENDSHIPTYPE(id) ON DELETE RESTRICT,
                                           UNIQUE (user_id, friend_id)
);
