package com.workhub.cs.repository;

import com.workhub.cs.entity.CsPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsPostRepository extends JpaRepository<CsPost,Long> {
}
