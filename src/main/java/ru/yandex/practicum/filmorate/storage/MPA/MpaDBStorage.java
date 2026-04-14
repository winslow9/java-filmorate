package ru.yandex.practicum.filmorate.storage.MPA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDBStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Autowired
    public MpaDBStorage(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Optional<MPA> findById(int id) {
        String sql = "SELECT id, name FROM DICT_MPA WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MPA> findAll() {
        String sql = "SELECT id, name FROM DICT_MPA ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }
}