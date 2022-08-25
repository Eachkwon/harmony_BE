package com.example.harmony.domain.community.repository;

import com.example.harmony.domain.community.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Slice<Post> findAllByCategory(String category, Pageable pageable);
}