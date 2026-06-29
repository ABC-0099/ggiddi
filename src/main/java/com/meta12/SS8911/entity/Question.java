package com.meta12.SS8911.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 200)
    private String subject;
    private String content;
    private LocalDateTime createDate;
//    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
//    private List<Answer> answerList;
    @ManyToOne
    private SiteUser author;

    public boolean isRecentPost() {
        return createDate.isAfter(LocalDateTime.now().minusDays(2));
    }
}
