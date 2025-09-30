package com.back.domain.chatbot.repository;

import com.back.domain.chatbot.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    List<ChatConversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatConversation> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}