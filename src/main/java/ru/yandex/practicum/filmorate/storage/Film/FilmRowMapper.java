package ru.yandex.practicum.filmorate.storage.Film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        int rateId = rs.getInt("rate_id");

        MPA mpa = null;
        if (!rs.wasNull()) {
            mpa = MPA.fromId(rateId);
        }

        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .rate(mpa)
                .genre(new ArrayList<>())  // ← используем ArrayList вместо List
                .likes(new HashSet<>())
                .build();
    }
}