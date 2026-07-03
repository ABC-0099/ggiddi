package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QuizAdminDTO;
import com.meta12.SS8911.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quiz")
@RequiredArgsConstructor
public class AdminQuizApiController {

    private final QuizService quizService;

    @PostMapping
    public QuizAdminDTO create(@RequestBody QuizAdminDTO dto) {
        return quizService.create(dto);
    }

    @PutMapping("/{id}")
    public QuizAdminDTO update(@PathVariable Long id, @RequestBody QuizAdminDTO dto) {
        return quizService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        quizService.delete(id);
    }
}