package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.AttendanceDTO;
import com.meta12.SS8911.entity.Attendance;
import com.meta12.SS8911.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}