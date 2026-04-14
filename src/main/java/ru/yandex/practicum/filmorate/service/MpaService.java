package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.MPA.MpaStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<MpaDto> getAllMpa() {
        return mpaStorage.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MpaDto getMpaById(int id) {
        MPA mpa = mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA с id " + id + " не найден"));
        return convertToDto(mpa);
    }

    private MpaDto convertToDto(MPA mpa) {
        return new MpaDto(mpa.getId(), mpa.getName());
    }
}