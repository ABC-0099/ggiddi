package com.meta12.SS8911.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.meta12.SS8911.dto.EventCreateRequestDto;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final Cloudinary cloudinary; // ← uploadPath/saveFile 대신 이걸로 업로드


    // 이벤트 등록
    public void create(
            EventCreateRequestDto dto,
            MultipartFile posterFile,
            MultipartFile thumbnailFile
    ) {

        Event event = dto.toEntity();

        // 포스터 저장 (Cloudinary)
        if (posterFile != null && !posterFile.isEmpty()) {
            String url = uploadToCloudinary(posterFile);
            event.setPoster(url);
        }

        // 썸네일 저장 (Cloudinary)
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String url = uploadToCloudinary(thumbnailFile);
            event.setThumbnail(url);
        }

        eventRepository.save(event);
    }



    public List<Event> findAll(){
        List<Event> events = eventRepository.findAll();

        events.sort(Comparator
                .comparing((Event e) -> statusPriority(e.getStatus()))
                .thenComparing(Event::getCreatedDate, Comparator.reverseOrder()));

        return events;
    }



    public Event findById(Long id){

        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음"));
    }




    // 이벤트 수정
    public void update(
            Long id,
            EventCreateRequestDto dto,
            MultipartFile posterFile,
            MultipartFile thumbnailFile
    ){

        Event event = findById(id);

        // 기본 정보 수정
        event.setTitle(dto.getTitle());
        event.setContent(dto.getContent());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setStatus(dto.getStatus());

        // 새 포스터 선택했을 때만 변경 (Cloudinary)
        if (posterFile != null && !posterFile.isEmpty()) {
            String url = uploadToCloudinary(posterFile);
            event.setPoster(url);
        }

        // 새 썸네일 선택했을 때만 변경 (Cloudinary)
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String url = uploadToCloudinary(thumbnailFile);
            event.setThumbnail(url);
        }

        eventRepository.save(event);
    }



    // 이벤트 삭제
    public void delete(Long id){

        eventRepository.deleteById(id);
    }




    // 실제 파일 업로드 (로컬 저장 대신 Cloudinary, secure_url 리턴)
    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "image")
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }
    private int statusPriority(String status) {
        return switch (status) {
            case "ONGOING" -> 0;
            case "UPCOMING" -> 1;
            case "ENDED" -> 2;
            default -> 3;
        };
    }
}