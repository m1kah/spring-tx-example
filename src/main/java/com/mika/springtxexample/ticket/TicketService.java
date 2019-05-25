package com.mika.springtxexample.ticket;

import com.mika.springtxexample.event.Event;
import com.mika.springtxexample.event.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Transactional
@RequiredArgsConstructor
public class TicketService {
    final TicketRepository ticketRepository;
    final EventRepository eventRepository;

    public List<Ticket> readAllBooked() {
        return ticketRepository.readAllBooked();
    }

    public Ticket createTicket(String show) {
        int ticketId = ticketRepository.bookTicket(makeTicket(show));
        eventRepository.createEvent(Event.booked());
        return ticketRepository.readBookedTicket(ticketId);
    }

    private Ticket makeTicket(@RequestParam String show) {
        return new Ticket()
                .setShow(show)
                .setLocation("here")
                .setStart(ZonedDateTime.now());
    }

}
