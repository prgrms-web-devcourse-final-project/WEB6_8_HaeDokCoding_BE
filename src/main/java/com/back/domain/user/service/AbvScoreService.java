package com.back.domain.user.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbvScoreService {

    private final UserRepository userRepository;

    private static final double POST_SCORE = 0.5;
    private static final double COMMENT_SCORE = 0.2;
    private static final double LIKE_SCORE = 0.1;
    private static final double KEEP_SCORE = 0.1;

    @Transactional
    public void awardForPost(Long userId) {
        addScore(userId, POST_SCORE);
    }

    @Transactional
    public void revokeForPost(Long userId) {
        addScore(userId, -POST_SCORE);
    }

    @Transactional
    public void awardForComment(Long userId) {
        addScore(userId, COMMENT_SCORE);
    }

    @Transactional
    public void revokeForComment(Long userId) {
        addScore(userId, -COMMENT_SCORE);
    }

    @Transactional
    public void awardForLike(Long userId) {
        addScore(userId, LIKE_SCORE);
    }

    @Transactional
    public void revokeForLike(Long userId) {
        addScore(userId, -LIKE_SCORE);
    }

    @Transactional
    public void awardForKeep(Long userId) {
        addScore(userId, KEEP_SCORE);
    }

    @Transactional
    public void revokeForKeep(Long userId) {
        addScore(userId, -KEEP_SCORE);
    }

    @Transactional
    public void revokeForKeep(Long userId, int count) {
        if (count <= 0) {
            return;
        }
        addScore(userId, -KEEP_SCORE * count);
    }

    private void addScore(Long userId, double delta) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(404, "사용자를 찾을 수 없습니다."));

        Double current = user.getAbvDegree();
        if (current == null) current = 5.0; // 신규 사용자는 5%부터 시작

        double next = clamp(current + delta, 0.0, 100.0);
        user.setAbvDegree(next);

        userRepository.save(user);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
