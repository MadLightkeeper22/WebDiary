package com.springboot.controller;

import com.springboot.domain.Diary;
import com.springboot.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    // 일기 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("diaries", diaryService.getDiaryList());
        return "diary/list";
    }

    // 작성 폼
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("diary", new Diary());
        return "diary/form";
    }

    // 일기 저장 (여러 장의 이미지 업로드 가능)
    @PostMapping
    public String create(@Valid @ModelAttribute Diary diary,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) throws Exception {

        // 유효성 검사 실패 -> 다시 폼으로
        if (bindingResult.hasErrors()) {
            return "diary/form";
        }

        // 이미지 업로드 처리
        saveImages(diary, imageFiles);

        diaryService.saveDiary(diary);
        return "redirect:/diary";
    }

    // 일기 상세보기
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("diary", diaryService.getDiary(id));
        return "diary/detail";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Diary diary = diaryService.getDiary(id);
        model.addAttribute("diary", diary);
        return "diary/edit";
    }

    // 수정 폼
    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute Diary formDiary,
                         BindingResult bindingResult,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "deleteImages", required = false) List<String> deleteImages
    ) throws Exception {

        if (bindingResult.hasErrors()) {
            return "diary/edit";
        }

        Diary diary = diaryService.getDiary(id);

        // 제목/내용 수정
        diary.setTitle(formDiary.getTitle());
        diary.setContent(formDiary.getContent());

        //1) 삭제 요청된 기존 사진 제거
        if (deleteImages != null && !deleteImages.isEmpty()) {
            diary.getImagePaths().removeAll(deleteImages);

            // 선택 옵션: 실제 파일까지 삭제하고 싶으면 아래 코드 유지
            for (String delPath : deleteImages) {
                if (delPath == null) continue;
                String fileName = delPath.replace("/images/", "");
                Path filePath = Paths.get(uploadDir, fileName);
                try {
                    Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    // 파일 삭제 실패해도 앱이 죽지는 않게 무시
                }
            }
        }

        //2) 새로 추가된 이미지 저장
        saveImages(diary, imageFiles);

        diaryService.saveDiary(diary);
        return "redirect:/diary/" + id;
    }

    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        diaryService.deleteDiary(id);
        return "redirect:/diary";
    }

    // 날짜 검색
    @GetMapping("/searchByDate")
    public String searchByDate(
            @RequestParam(name = "date", required = false) String dateString,
            Model model) {

        if (dateString == null || dateString.isBlank()) {
            model.addAttribute("dateError", "날짜를 입력하시오.");
            model.addAttribute("diaries", diaryService.getDiaryList());
            return "diary/list";
        }

        LocalDate date = LocalDate.parse(dateString);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Diary> diaries = diaryService.getDiariesByDate(startOfDay, endOfDay);

        model.addAttribute("diaries", diaries);
        model.addAttribute("searchDate", dateString);

        return "diary/dateResult";
    }

    // 이미지 저장 공통 메서드
    private void saveImages(Diary diary, List<MultipartFile> imageFiles) throws Exception {

        if (imageFiles == null) return;

        for (MultipartFile imageFile : imageFiles) {

            if (imageFile == null || imageFile.isEmpty()) continue;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = imageFile.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFileName = UUID.randomUUID() + ext;

            Path filePath = uploadPath.resolve(storedFileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            diary.getImagePaths().add("/images/" + storedFileName);
        }
    }
}
