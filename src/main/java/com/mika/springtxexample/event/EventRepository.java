package com.mika.springtxexample.event;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.transaction.Transactional;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class EventRepository {
    final NamedParameterJdbcTemplate jdbcTemplate;
    boolean failFlag = true;

    public List<Event> readAll() {
        return jdbcTemplate
                .query("select * from events order by occurred", this::rowToEvent);
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public Integer createEvent(Event event) {
        if (failFlag) {
            throw new RuntimeException("synthetic failure");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", event.getName());
        params.put("occurred", Timestamp.valueOf(event.getOccurred()));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "insert into events (name, occurred) values (:name, :occurred)",
                new MapSqlParameterSource(params),
                keyHolder);
        return keyHolder.getKey().intValue();
    }

    @SneakyThrows
    private Event rowToEvent(ResultSet rs, int row) {
        return new Event()
                .setId(rs.getInt("id"))
                .setName(rs.getString("name"))
                .setOccurred(rs.getTimestamp("occurred").toLocalDateTime());
    }

    public void setFailFlag(boolean failFlag) {
        this.failFlag = failFlag;
    }
}
