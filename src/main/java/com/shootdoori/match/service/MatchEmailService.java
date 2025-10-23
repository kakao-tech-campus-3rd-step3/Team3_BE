package com.shootdoori.match.service;

import com.shootdoori.match.entity.match.Match;
import com.shootdoori.match.entity.match.request.MatchRequest;
import com.shootdoori.match.entity.match.waiting.MatchWaiting;
import com.shootdoori.match.entity.team.Team;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class MatchEmailService {

    private final MailService mailService;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm");

    public MatchEmailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void sendMatchRequestEmail(Team requestTeam, MatchWaiting targetWaiting) {
        String receiverEmail = targetWaiting.getTeam().getCaptain().getEmail();

        String formattedDate = targetWaiting.getPreferredDate().format(DATE_FORMATTER);
        String formattedStart = targetWaiting.getPreferredTimeStart().format(TIME_FORMATTER);
        String formattedEnd = targetWaiting.getPreferredTimeEnd().format(TIME_FORMATTER);

        String subject = String.format("[슛두리 매치 신청] %s 팀이 매치를 신청했습니다!",
            requestTeam.getTeamName());
        String content = String.format(
            "%s 팀이 %s %s~%s에 매치를 신청했습니다.\n\n" +
                "경기 장소: %s\n\n",
            requestTeam.getTeamName(),
            formattedDate,
            formattedStart,
            formattedEnd,
            targetWaiting.getPreferredVenue()
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }

    public void sendMatchAcceptEmail(Match match) {
        String receiverEmail = match.getMatchRequestTeam().getCaptain().getEmail();

        String formattedDate = match.getMatchDate().format(DATE_FORMATTER);
        String formattedStart = match.getMatchTime().format(TIME_FORMATTER);

        String subject = String.format("[슛두리 매치 수락] %s 팀이 매치를 수락했습니다!",
            match.getMatchCreateTeam().getTeamName());
        String content = String.format(
            "%s 팀이 매치를 수락했습니다!\n\n" +
                "경기 일정: %s %s\n" +
                "경기 장소: %s\n\n" +
                "매치가 확정되었습니다. 즐거운 경기 되세요!",
            match.getMatchCreateTeam().getTeamName(),
            formattedDate,
            formattedStart,
            match.getVenue()
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }

    public void sendMatchRejectedEmail(MatchRequest rejectedRequest) {
        String receiverEmail = rejectedRequest.getRequestTeam().getCaptain().getEmail();

        MatchWaiting waiting = rejectedRequest.getMatchWaiting();
        String formattedDate = waiting.getPreferredDate().format(DATE_FORMATTER);
        String formattedStart = waiting.getPreferredTimeStart().format(TIME_FORMATTER);
        String formattedEnd = waiting.getPreferredTimeEnd().format(TIME_FORMATTER);

        String subject = String.format(
            "[슛두리 매치 거절] %s 팀에 대한 매치 요청이 거절되었습니다.",
            waiting.getTeam().getTeamName()
        );

        String content = String.format(
            "안녕하세요, 슛두리입니다.\n\n" +
                "아쉽게도 %s %s~%s에 신청하신 매치는 다른 팀과 확정되어 거절되었습니다.\n\n" +
                "다른 매치를 다시 신청해보세요!\n\n" +
                "감사합니다.",
            formattedDate,
            formattedStart,
            formattedEnd
        );

        mailService.sendEmail(receiverEmail, subject, content);
    }
}