package ru.yandex.practicum.filmorate.storage.MPA;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MPA> {

    @Override
    public MPA mapRow(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");

        MPA mpa = MPA.fromId(id);

        if (mpa == null && name != null) {
            for (MPA enumMpa : MPA.values()) {
                if (enumMpa.getName().equals(name)) {
                    mpa = enumMpa;
                    break;
                }
            }
        }

        return mpa;
    }
}