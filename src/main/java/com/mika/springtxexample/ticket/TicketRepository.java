package com.mika.springtxexample.ticket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.transaction.Transactional;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TicketRepository {
    final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Ticket> readAllBooked() {
        return jdbcTemplate
                .query("select * from tickets_booked order by id", this::rowToTicket);
    }

    public Ticket readBookedTicket(int id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        return jdbcTemplate.queryForObject("select * from tickets_booked where id = :id", params, this::rowToTicket);
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public Integer bookTicket(Ticket ticket) {
        Map<String, Object> params = new HashMap<>();
        params.put("show", ticket.getShow());
        params.put("location", ticket.getLocation());
        params.put("start", Timestamp.valueOf(ticket.getStart().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate
                .update("insert into tickets_booked (show, location, start) values (:show, :location, :start)",
                        new MapSqlParameterSource(params),
                        keyHolder);
        return keyHolder.getKey().intValue();
    }

    @SneakyThrows
    private Ticket rowToTicket(ResultSet rs, int row) {
        return new Ticket()
                .setId(rs.getInt("id"))
                .setShow(rs.getString("show"))
                .setLocation(rs.getString("location"))
                .setStart(getZonedDateTime(rs, "start"));
    }

    @SneakyThrows
    private ZonedDateTime getZonedDateTime(ResultSet rs, String column) {
        Timestamp timestamp = rs.getTimestamp(column);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault());
    }
}
