package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.QnaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaFileRepository extends JpaRepository<QnaFile, Long> {
    List<QnaFile> findByQna(Qna qna);
    void deleteByQna(Qna qna);
}