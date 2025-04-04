package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findById(int id);

    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM Image")
    int findLatestId();
}