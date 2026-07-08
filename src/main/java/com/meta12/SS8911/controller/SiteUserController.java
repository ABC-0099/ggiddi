package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.AttendanceDTO;
import com.meta12.SS8911.dto.SiteUserDTO;
import com.meta12.SS8911.dto.SiteUserEditDTO;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.*;
import com.meta12.SS8911.service.AttendanceService; // 이 줄이 있는지 확인하세요
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.DayOfWeek;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SiteUserController {
    private final SiteUserService siteUserService;
    private final CommunityService communityService;
    private final CommentService commentService;
    private final QnaService qnaService;
    private final AttendanceService attendanceService;

    @GetMapping("/siteUser/chuga")
    public String chugaForm(SiteUserDTO siteUserDTO) {
        return "siteUser/chuga";
    }

    @PostMapping("/siteUser/chugaProc")
    public String chugaProc(@Valid SiteUserDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "siteUser/chuga";
        if (!dto.getPassword().equals(dto.getPasswordChk())) {
            bindingResult.rejectValue("passwordChk", "error", "비밀번호가 일치하지 않습니다.");
            return "siteUser/chuga";
        }
        siteUserService.chugaProc(dto);
        return "redirect:/siteUser/login";
    }

    @GetMapping("/siteUser/profile/{username}")
    public String profile(@PathVariable("username") String username, Model model) {
        SiteUser user = siteUserService.getUserByUsername(username);
        model.addAttribute("siteUser", user);
        return "siteUser/view";
    }

    @GetMapping("/siteUser/login")
    public String login() {
        return "siteUser/login";
    }

    @GetMapping("/siteUser/mypage")
    public String mypage(Model model, Principal principal,
                         @RequestParam(defaultValue = "0") int postPage,
                         @RequestParam(defaultValue = "0") int commentPage,
                         @RequestParam(defaultValue = "0") int inquiryPage) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        String username = principal.getName();

        Pageable postPageable = PageRequest.of(postPage, 5);
        Page<Community> myPosts = communityService.getPostsByAuthor(user, postPageable);

        Pageable commentPageable = PageRequest.of(commentPage, 5);
        Page<Comment> myComments = commentService.getCommentsByAuthor(user, commentPageable);

        Pageable qnaPageable = PageRequest.of(inquiryPage, 5);
        Page<Qna> myInquiries = qnaService.getMyQnas(user, qnaPageable);

        model.addAttribute("siteUser", user);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", myComments);
        model.addAttribute("myInquiries", myInquiries);
        model.addAttribute("heatmapData", siteUserService.getHeatmapData(user));

        // ▼▼▼ 출석 관련 데이터 추가 ▼▼▼
        int currentStreak = attendanceService.getCurrentStreak(username);
        boolean[] weeklyAttendance = attendanceService.getWeeklyAttendance(username);
        int lastWeekAttendedCount = attendanceService.getLastWeekAttendedCount(username);

        int attendedCount = 0;
        for (boolean attended : weeklyAttendance) {
            if (attended) {
                attendedCount++;
            }
        }

        model.addAttribute("currentStreak", currentStreak);
        model.addAttribute("weeklyAttendance", weeklyAttendance);
        model.addAttribute("attendedCount", attendedCount);
        model.addAttribute("lastWeekAttendedCount", lastWeekAttendedCount);
        // ▲▲▲ 출석 관련 데이터 추가 끝 ▲▲▲

        return "siteUser/mypage";
    }

    @GetMapping("/siteUser/edit")
    public String editForm(Model model, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        SiteUserEditDTO dto = new SiteUserEditDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setBirth(user.getBirth());
        dto.setNationality(user.getNationality());

        System.out.println("siteUser : " + user);

        model.addAttribute("siteUser", user);
        model.addAttribute("siteUserEditDTO", dto);

        return "siteUser/edit";
    }

    // 수정 완료: 중복된 editProc 메서드를 하나로 통합
    @PostMapping("/siteUser/editProc")
    public String editProc(
            @Valid @ModelAttribute("siteUserEditDTO") SiteUserEditDTO dto,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes rttr) {

        if (bindingResult.hasErrors()) {

            System.out.println("===== Validation Errors =====");

            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println(
                        "field = " + error.getField()
                                + ", rejectedValue = " + error.getRejectedValue()
                                + ", message = " + error.getDefaultMessage()
                );
            });

            SiteUser user = siteUserService.getUserByUsername(principal.getName());
            model.addAttribute("siteUser", user);

            return "siteUser/edit";
        }

        try {
            siteUserService.editProc(principal.getName(), dto);
            rttr.addFlashAttribute("successMsg", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/siteUser/edit";
        }

        return "redirect:/siteUser/mypage";
    }

    @PostMapping("/siteUser/withdraw")
    public String withdraw(@RequestParam String currentPassword,
                           Principal principal,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        try {
            siteUserService.withdraw(principal.getName(), currentPassword);
        } catch (IllegalStateException e) {
            return "redirect:/siteUser/mypage?withdrawError=true";
        }

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/?withdrawn=true";
    }

    @GetMapping("/api/attendance")
    @ResponseBody
    public List<AttendanceDTO> getAttendance(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            Principal principal) {

        // 1. 위에서 주입받은 객체인 attendanceService를 사용하세요!
        return attendanceService.getMonthlyData(principal.getName(), year, month);
    }

    @GetMapping("/api/attendance/weekly")
    @ResponseBody
    public Map<String, Object> getWeeklyAttendanceApi(
            @RequestParam(defaultValue = "0") int weekOffset,
            Principal principal) {

        String username = principal.getName();
        boolean[] weeklyAttendance = attendanceService.getWeeklyAttendance(username, weekOffset);

        int attendedCount = 0;
        for (boolean attended : weeklyAttendance) {
            if (attended) {
                attendedCount++;
            }
        }

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate sunday = monday.plusDays(6);
        String weekLabel = monday.getMonthValue() + "." + monday.getDayOfMonth()
                + " - " + sunday.getMonthValue() + "." + sunday.getDayOfMonth();

        Map<String, Object> result = new HashMap<>();
        result.put("weeklyAttendance", weeklyAttendance);
        result.put("attendedCount", attendedCount);
        result.put("weekLabel", weekLabel);
        result.put("weekOffset", weekOffset);
        result.put("isCurrentWeek", weekOffset == 0);
        return result;
    }
}
