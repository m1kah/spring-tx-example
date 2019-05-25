package com.mika.springtxexample.ticket;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class Ticket {
    private int id;
    private String show;
    private ZonedDateTime start;
    private String location;
}
