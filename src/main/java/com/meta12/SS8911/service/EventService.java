package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.EventCreateRequestDto;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;


    // 업로드 폴더 위치
    // 프로젝트 실행 위치(user.dir) 기준 상대경로로 변경 - 팀원 PC마다 사용자명/폴더명이 달라서
    // "C:/Users/user/Documents/GitHub/ggiddi/..." 처럼 절대경로로 박아두면 다른 PC에서 실행 시
    // 해당 경로가 없어서 파일 저장이 FileNotFoundException으로 실패함.
    // IDE(IntelliJ)나 gradle/mvn으로 프로젝트 루트에서 실행한다는 전제하에 동작.
    private final String uploadPath =
            System.getProperty("user.dir") + "/src/main/resources/static/images/";


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



    // 이벤트 삭제
    public void delete(Long id){

        eventRepository.deleteById(id);
    }




    // 실제 파일 저장
    private void saveFile(MultipartFile file){

        try{

            File dir = new File(uploadPath);

            if(!dir.exists()){
                boolean created = dir.mkdirs();
                if(!created && !dir.exists()){
                    throw new RuntimeException("업로드 폴더 생성 실패: " + dir.getAbsolutePath());
                }
            }


            File saveFile =
                    new File(uploadPath + file.getOriginalFilename());


            file.transferTo(saveFile);


        }catch(Exception e){

            throw new RuntimeException(e);

        }

    }

    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

}