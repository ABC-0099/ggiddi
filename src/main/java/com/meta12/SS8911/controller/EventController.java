package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.EventService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final SiteUserService siteUserService;
    private final EventService eventService;

    @GetMapping
    public String main(Model model) {
        List<Event> events = eventService.findAll();
        model.addAttribute("events", events);
        return "event/event";
    }

    @GetMapping("/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {

        Event event;
        try {
            event = eventService.findById(id);
        } catch (IllegalArgumentException e) {
            return "redirect:/event";
        }

        model.addAttribute("eventId", id);
        model.addAttribute("event", event);

        return "event/eventDetail";
    }

}