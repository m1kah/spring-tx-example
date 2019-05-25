package com.mika.springtxexample.ticket;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("tickets")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TicketController {
    final TicketService ticketService;

    @GetMapping()
    List<Ticket> getAll() {
        return ticketService.readAllBooked();
    }

    @PostMapping()
    ResponseEntity<?> bookTicket(@RequestParam String show) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(show));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse().setError(e.getMessage()));
        }
    }

    @Data
    @Accessors(chain = true)
    static class ErrorResponse {
        private String error;
    }
}
