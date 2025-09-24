package com.back.domain.cocktail.comment.repository;

import com.back.domain.post.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CocktailCommentRepository extends JpaRepository<Comment, Long> {

}
