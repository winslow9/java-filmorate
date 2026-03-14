# java-filmorate
![ER-диаграмма](https://downloader.disk.yandex.ru/preview/9c01fd204467374f4a01709929756faf2e43dc33696f57ba7032f390adee7a0b/69b57911/RjkYJBcGRxDvxct3O3U3fc-IsLkCq9Cm5AhIwLW9_zFaRXGR7VTG8uiVJf98q-_3-Dr_vbUVnaKoxq_evUV65Q%3D%3D?uid=0&filename=erd.png&disposition=inline&hash=&limit=0&content_type=image%2Fpng&owner_uid=0&tknv=v3&size=2048x2048)

**Хотелось бы прокомментировать пару моментов:**
1) Никто не просил, но я вставил время создания и изменения записей. Это хорошая практика;
2) Время создания и изменения записей нет в словарях, т.к. это излишне;
3) Friendship.proved_friendship будет принимать значение true только для подтверждённых заявок на дружбу.

   

**Запрос, который покажет самый любимый жанр друзей указанного пользователя (например, для пользователя с id = 1):**
```
SELECT 
    dfg.id AS genre_id,
    dfg.name AS genre_name,
    COUNT(*) AS likes_count
FROM dict_FilmGenres dfg
INNER JOIN link_FilmGenre lfg ON dfg.id = lfg.genre_id
INNER JOIN link_FilmLikes lfl ON lfg.film_id = lfl.film_id
INNER JOIN Friendship f ON 
    (f.user_id = lfl.user_id OR f.friend_id = lfl.user_id)
WHERE (f.user_id = 1 OR f.friend_id = 1)  -- пользователь с id = 1
  AND f.proved_friendship = true
  AND lfl.user_id != 1  -- исключить самого пользователя
GROUP BY dfg.id, dfg.name
ORDER BY likes_count DESC
LIMIT 1;
```
