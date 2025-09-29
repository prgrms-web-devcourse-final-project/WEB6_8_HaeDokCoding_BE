package com.back.domain.chatbot.repository;

import com.back.domain.chatbot.entity.ChatConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Page<ChatConversation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ChatConversation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}