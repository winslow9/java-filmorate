# java-filmorate

## ER-диаграмма

![ER-диаграмма](erd.png)


## Комментарии к схеме базы данных

Хотелось бы прокомментировать пару моментов:

- Никто не просил, но я вставил время создания и изменения записей. Это хорошая практика;
- Время создания и изменения записей отсутствует в словарях, так как это излишне;
- В tbl.Friendship UNIQ(user_id, friend_id);
- `Friendship.proved_friendship` будет принимать значение `true` только для подтверждённых заявок на дружбу.

## Пример SQL-запроса

Запрос, который покажет самый любимый жанр друзей указанного пользователя (например, для пользователя с `id = 1`):

```sql
SELECT
    dfg.id AS genre_id,
    dfg.name AS genre_name,
    COUNT(*) AS likes_count
FROM dict_FilmGenres dfg
INNER JOIN link_FilmGenre lfg ON dfg.id = lfg.genre_id
INNER JOIN link_FilmLikes lfl ON lfg.film_id = lfl.film_id
INNER JOIN Friendship f ON (f.user_id = lfl.user_id OR f.friend_id = lfl.user_id)
WHERE (f.user_id = 1 OR f.friend_id = 1)  -- пользователь с id = 1
    AND f.proved_friendship = true
    AND lfl.user_id != 1  -- исключить самого пользователя
GROUP BY dfg.id, dfg.name
ORDER BY likes_count DESC
LIMIT 1;
```
```plantuml
@startuml

!define table(x) class x << (T,#FFAAAA) >>

' Таблицы
table(User) {
    + id: LONG [PK]
    --
    login: VARCHAR
    name: VARCHAR
    email: VARCHAR
    birthday: DATE
create_time: TIMESTAMP
update_time: TIMESTAMP
}

table(Film) {
    + id: LONG [PK]
    --
    name: VARCHAR
    description: VARCHAR
    release_date: DATE
    duration: INTEGER
    rate_id: INTEGER [FK] - MPA(id)
create_time: TIMESTAMP
update_time: TIMESTAMP
}

table(dict_MPA) {
    + id: INTEGER [PK]
    --
    name: VARCHAR
}

table(dict_FilmGenres) {
    + id: INTEGER [PK]
    --
    name: VARCHAR
}

table(dict_FriendshipType) {
    + id: INTEGER [PK]
    --
    name: VARCHAR
}

' Связующие таблицы
table(link_FilmGenre) {
    + id: LONG [PK]
    + film_id: LONG [FK]
    + genre_id: INTEGER [FK]
--
create_time: TIMESTAMP
update_time: TIMESTAMP
}

table(link_FilmLikes) {
    + id: LONG [PK]
    + film_id: LONG [FK]
    + user_id: LONG [FK]
--
create_time: TIMESTAMP
update_time: TIMESTAMP
}

table(Friendship) {
    + id: LONG [PK]
    + user_id: LONG [FK]
    + friend_id: LONG [FK]
--
FriendshipType: INTEGER [FK]
create_time: TIMESTAMP
update_time: TIMESTAMP
}

' Связи
User ||--o{ Friendship 
User ||--o{ link_FilmLikes 
Film ||--o{ link_FilmLikes 
Film ||--o{ link_FilmGenre
dict_FriendshipType||-o{ Friendship 
dict_MPA ||--o{ Film 
dict_FilmGenres ||--o{ link_FilmGenre

@enduml
```
