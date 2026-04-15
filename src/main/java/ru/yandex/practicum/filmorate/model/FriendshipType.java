package ru.yandex.practicum.filmorate.model;

public enum FriendshipType {
    CONFIRMED(1, "Подтвержденная"),
    UNCONFIRMED(2, "Неподтвержденная");

    private final int id;
    private final String name;

    FriendshipType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static FriendshipType fromId(int id) {
        for (FriendshipType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}