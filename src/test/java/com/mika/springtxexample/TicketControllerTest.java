package com.mika.springtxexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mika.springtxexample.ticket.Ticket;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AssertionErrors;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Slf4j
public class TicketControllerTest {
    @LocalServerPort
    int port;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired @Qualifier("h2")
    DataSource ticketDataSource;
    @Autowired @Qualifier("hsql")
    DataSource eventDataSource;
    JdbcTemplate ticketJdbcTemplate;
    JdbcTemplate eventJdbcTemplate;

    @Before
    public void setup() {
        initDb();
        RestAssured.port = port;
    }

    @SneakyThrows
    private void initDb() {
        ticketJdbcTemplate = new JdbcTemplate(ticketDataSource);
        ticketJdbcTemplate.execute("create table if not exists tickets_booked (" +
                "  id identity primary key," +
                "  show varchar(100)," +
                "  location varchar(100)," +
                "  start timestamp" +
                ");");
        ticketJdbcTemplate.execute("delete from tickets_booked");

        eventJdbcTemplate= new JdbcTemplate(eventDataSource);
        eventJdbcTemplate.execute("create table if not exists events (" +
                "  id identity primary key," +
                "  name varchar(100)," +
                "  occurred timestamp" +
                ");");
        eventJdbcTemplate.execute("delete from events");
    }

    @Ignore("ignored, happy case that would fail because of exception that we are throwing on purpose")
    @Test
    public void bookTicket() {
        log.info("tickets: {}", readTickets());
        post("/tickets?show=the_greatest_show_on_earth")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .body("id", is("1"))
                .body("start", new TimeMatcher())
                .body("start", is("here"));
        List<Ticket> tickets = readTickets();
        log.info("tickets: {}", tickets);
        assertThat(tickets.size(), is(1));
        assertEventCount(1);
    }

    @Test
    public void rollbackWhenSecondInsertFails() {
        log.info("tickets: {}", readTickets());
        post("/tickets?show=the_greatest_show_on_earth")
                .then()
                .log().body()
                .assertThat()
                .statusCode(500);
        List<Ticket> tickets = readTickets();
        log.info("tickets: {}", tickets);
        assertThat(tickets.size(), is(0));
        assertEventCount(0);
    }

    private List<Ticket> readTickets() {
        TypeRef<List<Ticket>> listType = new TypeRef<List<Ticket>>() {};
        return get("/tickets").then()
                .log()
                .body()
                .extract().as(listType);
    }

    private void isAboutNow(String value) {
        LocalDateTime start = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime now = LocalDateTime.now();
        AssertionErrors.assertTrue(
                "start time is not now +/- 1 minutes, was=" + value,
                start.isAfter(now.minusMinutes(1)) && start.isBefore(now.plusMinutes(1)));
    }

    private void assertEventCount(int count) {
        Integer rows = eventJdbcTemplate.queryForObject("select count(id) as count from events", (rs, row) -> rs.getInt("count"));
        log.info("event count is {}", rows);
        assertThat(rows, is(count));
    }

    static class TimeMatcher extends TypeSafeMatcher<String> {

        @Override
        protected boolean matchesSafely(String item) {
            try {
                LocalDateTime start = LocalDateTime.parse(item, DateTimeFormatter.ISO_DATE_TIME);
                LocalDateTime now = LocalDateTime.now();
                return start.isAfter(now.minusMinutes(1)) && start.isBefore(now.plusMinutes(1));
            } catch (RuntimeException e) {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Time was within allowed margin of +/- 1 minutes");
        }
    }
}
