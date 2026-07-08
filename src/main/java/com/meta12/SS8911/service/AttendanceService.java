package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.AttendanceDTO;
import com.meta12.SS8911.entity.Attendance;
import com.meta12.SS8911.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;
import com.meta12.SS8911.entity.SiteUser;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    // 1. 여기서 필드명을 정확히 확인하세요 (소문자 attendanceRepository)
    private final AttendanceRepository attendanceRepository;

    public List<AttendanceDTO> getMonthlyData(String username, int year, int month) {
        // 2. 여기서 호출할 때도 똑같이 소문자 객체를 사용합니다.
        List<Attendance> entities = attendanceRepository.findByUsernameAndYearAndMonth(username, year, month);

        return entities.stream()
                .map(a -> new AttendanceDTO(a.getDate().toString()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkAttendance(SiteUser user) {

        LocalDate today = LocalDate.now();

        // 오늘 이미 출석했는지 확인
        if (attendanceRepository.existsBySiteUserAndDate(user, today)) {
            return;
        }

        Attendance attendance = new Attendance();
        attendance.setSiteUser(user);
        attendance.setDate(today);

        attendanceRepository.save(attendance);
    }

    public boolean[] getWeeklyAttendance(String username) {
        return getWeeklyAttendance(username, 0);
    }

    // weekOffset: 0=이번 주, -1=지난주, -2=지지난주 ... (양수는 미래 주라 UI에서 막을 예정)
    public boolean[] getWeeklyAttendance(String username, int weekOffset) {

        LocalDate today = LocalDate.now();

        LocalDate monday = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate sunday = monday.plusDays(6);

        List<Attendance> list =
                attendanceRepository.findWeeklyAttendance(username, monday, sunday);

        boolean[] result = new boolean[7];

        for (Attendance attendance : list) {

            int index = attendance.getDate().getDayOfWeek().getValue() - 1;
            result[index] = true;

        }

        return result;
    }

    // ▼▼▼ 새로 추가한 메서드 ▼▼▼

    // 오늘부터 거슬러 올라가며 연속 출석일 계산
    public int getCurrentStreak(String username) {
        LocalDate cursor = LocalDate.now();
        int streak = 0;

        // 오늘 아직 출석 전이면 어제부터 계산 (오늘 출석 여부가 streak을 끊지 않도록)
        if (!attendanceRepository.existsBySiteUser_UsernameAndDate(username, cursor)) {
            cursor = cursor.minusDays(1);
        }

        while (attendanceRepository.existsBySiteUser_UsernameAndDate(username, cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    // 지난주(월~일) 출석일 수
    public int getLastWeekAttendedCount(String username) {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);

        List<Attendance> list =
                attendanceRepository.findWeeklyAttendance(username, lastMonday, lastSunday);

        return list.size();
    }

    // ▲▲▲ 새로 추가한 메서드 끝 ▲▲▲
}
