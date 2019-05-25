package com.mika.springtxexample.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Event {
    private int id;
    private String name;
    private LocalDateTime occurred;

    public static Event booked() {
        return new Event()
                .setName("ticket-booked")
                .setOccurred(LocalDateTime.now());
    }
}
