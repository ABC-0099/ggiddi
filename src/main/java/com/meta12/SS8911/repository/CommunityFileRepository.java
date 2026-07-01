package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.CommunityFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityFileRepository extends JpaRepository<CommunityFile, Long> {

    List<CommunityFile> findByCommunity(Community community);

    List<CommunityFile> findByCommunityAndFileType(Community community, String fileType);

    void deleteByCommunity(Community community);
}