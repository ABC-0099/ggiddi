package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EventCreateRequestDto {

    private String title;

    private String content;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String poster;

    private String thumbnail;

    public Event toEntity() {

        Event event = new Event();

        event.setTitle(this.title);
        event.setContent(this.content);
        event.setStartDate(this.startDate);
        event.setEndDate(this.endDate);
        event.setStatus(this.status);
        event.setPoster(this.poster);
        event.setThumbnail(this.thumbnail);

        return event;
    }

}