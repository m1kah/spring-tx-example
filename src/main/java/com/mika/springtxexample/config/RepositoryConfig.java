package com.mika.springtxexample.config;

import com.mika.springtxexample.event.EventRepository;
import com.mika.springtxexample.ticket.TicketRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RepositoryConfig {
    @Bean
    public TicketRepository ticketRepository(@Qualifier("h2") DataSource dataSource) {
        return new TicketRepository(new NamedParameterJdbcTemplate(dataSource));
    }

    @Bean
    public EventRepository eventRepository(@Qualifier("hsql") DataSource dataSource) {
        return new EventRepository(new NamedParameterJdbcTemplate(dataSource));
    }
}
