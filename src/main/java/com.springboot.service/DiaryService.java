package com.springboot.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.domain.Diary;
import com.springboot.repository.DiaryRepository;

@Service
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    public List<Diary> getDiaryList() {
        return diaryRepository.findAllByOrderByCreatedAtDesc();
    }

    public Diary getDiary(Long id) {
        return diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다. id=" + id));
    }

    public Diary saveDiary(Diary diary) {
        if (diary.getCreatedAt() == null) {
            diary.setCreatedAt(LocalDateTime.now());
        }
        return diaryRepository.save(diary);
    }

    public void deleteDiary(Long id) {
        diaryRepository.deleteById(id);
    }
    
    public List<Diary> getDiariesByDate(LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByCreatedAtBetween(start, end);
    }

}

