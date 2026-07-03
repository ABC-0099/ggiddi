package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.MockExamAdminDTO;
import com.meta12.SS8911.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mock")
@RequiredArgsConstructor
public class AdminMockExamApiController {

    private final MockExamService mockExamService;

    @PostMapping
    public MockExamAdminDTO create(@RequestBody MockExamAdminDTO dto) {
        return mockExamService.create(dto);
    }

    @PutMapping("/{id}")
    public MockExamAdminDTO update(@PathVariable Long id, @RequestBody MockExamAdminDTO dto) {
        return mockExamService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mockExamService.delete(id);
    }
}