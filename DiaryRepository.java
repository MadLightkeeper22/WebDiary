package com.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.domain.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 최신 글이 위로 오게 정렬
    List<Diary> findAllByOrderByCreatedAtDesc();
    //해당 일에 작성된 일기 찾기
    List<Diary> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
