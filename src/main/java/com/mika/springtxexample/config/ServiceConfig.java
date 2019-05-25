package com.mika.springtxexample.config;

import com.mika.springtxexample.event.EventRepository;
import com.mika.springtxexample.ticket.TicketRepository;
import com.mika.springtxexample.ticket.TicketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public TicketService ticketService(TicketRepository ticketRepository, EventRepository eventRepository) {
        return new TicketService(ticketRepository, eventRepository);
    }
}
