package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Answer;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}