package com.meta12.SS8911.service;

import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository  eventRepository;

    public void create(Event  event){
        eventRepository.save(event);
    }

    public List <Event> findAll(){
        return eventRepository.findAll();
    }
}