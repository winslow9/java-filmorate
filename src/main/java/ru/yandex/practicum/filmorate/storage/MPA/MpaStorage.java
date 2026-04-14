package ru.yandex.practicum.filmorate.storage.MPA;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

public interface MpaStorage {
    Optional<MPA> findById(int id);

    List<MPA> findAll();
}