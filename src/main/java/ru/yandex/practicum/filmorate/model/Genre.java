package ru.yandex.practicum.filmorate.model;

public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    MELODRAMA(3, "Мультфильм"),
    ACTION(4, "Триллер"),
    THRILLER(5, "Документальный"),
    HORROR(6, "Боевик");

    private final int id;
    private final String name;

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Genre fromId(int id) {
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        return null;
    }
}