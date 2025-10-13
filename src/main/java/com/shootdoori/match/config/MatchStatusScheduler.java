package com.shootdoori.match.config;

import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.MatchStatus;
import com.shootdoori.match.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class MatchStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchStatusScheduler.class);

    private final MatchRepository matchRepository;

    public MatchStatusScheduler(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void updateFinishedMatches() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalTime cutoffTime = now.minusHours(3);
        LocalDate cutoffDate;

        if (cutoffTime.isAfter(now)) {
            cutoffDate = today.minusDays(1);
        } else {
            cutoffDate = today;
        }

        List<Match> matches = matchRepository.findMatchesToFinish(cutoffDate, cutoffTime);

        if (!matches.isEmpty()) {
            matches.forEach(m -> m.updateStatus(MatchStatus.FINISHED));
            matchRepository.saveAll(matches);
            log.info("자동 종료된 경기 수: {}", matches.size());
        }
    }
}