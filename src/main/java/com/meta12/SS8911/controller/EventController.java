package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.entity.Eventcomment;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.EventCommentRepository;
import com.meta12.SS8911.service.EventService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final SiteUserService siteUserService;
    private final EventService eventService;
    private final EventCommentRepository eventCommentRepository;

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

        List<Eventcomment> comments = eventCommentRepository.findByEventOrderByCreateDateDesc(event);

        model.addAttribute("eventId", id);
        model.addAttribute("event", event);
        model.addAttribute("comments", comments);

        return "event/eventDetail";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        Event event;
        try {
            event = eventService.findById(id);
        } catch (IllegalArgumentException e) {
            return "redirect:/event";
        }

        SiteUser author = siteUserService.getUser(principal.getName());

        Eventcomment comment = new Eventcomment();
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        eventCommentRepository.save(comment);

        return "redirect:/event/" + id;
    }

}