package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum MPA {
    G(1, "G"),
    PG(2, "PG"),
    PG13(3, "PG-13"),
    R(4, "R"),
    NC17(5, "NC-17");

    private final int id;
    private final String name;

    MPA(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static MPA fromId(int id) {
        for (MPA mpa : values()) {
            if (mpa.id == id) {
                return mpa;
            }
        }
        return null;
    }

    @JsonCreator
    public static MPA fromJson(Object value) {
        if (value instanceof Integer) {
            MPA mpa = fromId((Integer) value);
            if (mpa == null) {
                throw new IllegalArgumentException("MPA with id " + value + " not found");
            }
            return mpa;
        }

        if (value instanceof String) {
            try {
                int id = Integer.parseInt((String) value);
                MPA mpa = fromId(id);
                if (mpa == null) {
                    throw new IllegalArgumentException("MPA with id " + id + " not found");
                }
                return mpa;
            } catch (NumberFormatException e) {
                for (MPA mpa : values()) {
                    if (mpa.name.equalsIgnoreCase((String) value)) {
                        return mpa;
                    }
                }
                throw new IllegalArgumentException("MPA with name " + value + " not found");
            }
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Object idObj = map.get("id");
            if (idObj instanceof Integer) {
                Integer id = (Integer) idObj;
                MPA mpa = fromId(id);
                if (mpa == null) {
                    throw new IllegalArgumentException("MPA with id " + id + " not found");
                }
                return mpa;
            }
        }

        throw new IllegalArgumentException("Invalid MPA format: " + value);
    }
}