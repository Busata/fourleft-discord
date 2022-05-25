package io.busata.fourleftdiscord.schedules;

import io.busata.fourleftdiscord.autoposting.AutoPostCommunityEventResultsService;
import io.busata.fourleftdiscord.autoposting.AutoPostClubResultsService;
import io.busata.fourleftdiscord.autoposting.AutoPosterAutomatedClubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPosterSchedule {
    private final AutoPostClubResultsService autoPostClubResultsService;
    private final AutoPostCommunityEventResultsService autoPostCommunityEventResultsService;

    private final AutoPosterAutomatedClubService autoPosterAutomatedClubService;

    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void postAutoSchedule() {
        log.info("Checking for new club entries.");
        autoPostClubResultsService.update();
        log.info("Club entries check complete.");
    }

    @Scheduled(cron = "0 21 12 * * *", zone="Europe/Brussels")
    public void updateChallenges() {
        log.info("Checking community event results.");
        autoPostCommunityEventResultsService.update();
        log.info("Community events check complete.");
    }

    @Scheduled(cron = "0 20 11 * * *", zone="Europe/Brussels")
    public void postAutomatedDailyClubResults() {
        log.info("Posting Daily results");
        autoPosterAutomatedClubService.postResults();
    }

    @Scheduled(cron = "0 1 10 * * *", zone="Europe/Brussels")
    public void postDailyChallengeInfo() {
        log.info("Posting Club new stage info");
        autoPosterAutomatedClubService.postNewStage();
    }


}
