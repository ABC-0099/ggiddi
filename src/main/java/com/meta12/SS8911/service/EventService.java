package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.EventCreateRequestDto;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;


    // 업로드 폴더 위치
    private final String uploadPath =
            "C:/Users/user/Documents/GitHub/ggiddi/src/main/resources/static/images/";


    // 이벤트 등록
    public void create(
            EventCreateRequestDto dto,
            MultipartFile posterFile,
            MultipartFile thumbnailFile
    ) {


        Event event = dto.toEntity();


        // 포스터 저장
        if(posterFile != null && !posterFile.isEmpty()) {

            String fileName = posterFile.getOriginalFilename();

            event.setPoster(fileName);

            saveFile(posterFile);
        }


        // 썸네일 저장
        if(thumbnailFile != null && !thumbnailFile.isEmpty()) {

            String fileName = thumbnailFile.getOriginalFilename();

            event.setThumbnail(fileName);

            saveFile(thumbnailFile);
        }


        eventRepository.save(event);
    }



    public List<Event> findAll(){

        return eventRepository.findAll();
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



        // 새 포스터 선택했을 때만 변경
        if(posterFile != null && !posterFile.isEmpty()) {

            String fileName = posterFile.getOriginalFilename();

            event.setPoster(fileName);

            saveFile(posterFile);
        }



        // 새 썸네일 선택했을 때만 변경
        if(thumbnailFile != null && !thumbnailFile.isEmpty()) {

            String fileName = thumbnailFile.getOriginalFilename();

            event.setThumbnail(fileName);

            saveFile(thumbnailFile);
        }



        eventRepository.save(event);
    }




    // 실제 파일 저장
    private void saveFile(MultipartFile file){

        try{

            File dir = new File(uploadPath);

            if(!dir.exists()){
                dir.mkdirs();
            }


            File saveFile =
                    new File(uploadPath + file.getOriginalFilename());


            file.transferTo(saveFile);


        }catch(Exception e){

            throw new RuntimeException(e);

        }

    }

}