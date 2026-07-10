package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.AttendanceDTO;
import com.meta12.SS8911.entity.Attendance;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public List<AttendanceDTO> getMonthlyData(String username, int year, int month) {
        return attendanceRepository.findByUsernameAndYearAndMonth(username, year, month)
                .stream()
                .map(a -> new AttendanceDTO(a.getDate().toString()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAttendance(SiteUser siteUser) {
        LocalDate today = LocalDate.now();

        // 1. 중복 출석 방지
        if (!attendanceRepository.existsBySiteUserAndDate(siteUser, today)) {
            Attendance attendance = new Attendance();
            attendance.setSiteUser(siteUser);
            attendance.setDate(today);
            attendanceRepository.save(attendance);
        }
    }

    public boolean[] getWeeklyAttendance(String username, int weekOffset) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate sunday = monday.plusDays(6);

        List<Attendance> list = attendanceRepository.findWeeklyAttendance(username, monday, sunday);

        boolean[] result = new boolean[7];
        for (Attendance attendance : list) {
            int index = attendance.getDate().getDayOfWeek().getValue() - 1;
            result[index] = true;
        }
        return result;
    }

    public int getCurrentStreak(String username) {
        LocalDate cursor = LocalDate.now();
        int streak = 0;

        if (!attendanceRepository.existsBySiteUser_UsernameAndDate(username, cursor)) {
            cursor = cursor.minusDays(1);
        }

        while (attendanceRepository.existsBySiteUser_UsernameAndDate(username, cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int getLastWeekAttendedCount(String username) {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);
        return attendanceRepository.findWeeklyAttendance(username, lastMonday, lastSunday).size();
    }
}