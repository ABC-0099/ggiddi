package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.MockExamBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MockExamBoxRepository extends JpaRepository<MockExamBox, Long> {

    List<MockExamBox> findByUserIdOrderBySolvedDateDesc(Long userId);

    long countByUserId(Long userId);

    long countByMockExamIdAndUserId(Long mockExamId, Long userId);

    Optional<MockExamBox> findTopByMockExamIdAndUserIdOrderBySolvedDateDesc(Long mockExamId, Long userId);

    @Query("select coalesce(avg(cast(b.score as double) / b.total), 0) " +
            "from MockExamBox b where b.user.id = :userId and b.total > 0")
    Double findAvgAccuracyByUserId(Long userId);
}