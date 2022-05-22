package io.busata.fourleftdiscord.autoposting;

import discord4j.common.util.Snowflake;
import io.busata.fourleftdiscord.messages.DiscordMessageFacade;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
import io.busata.fourleftdiscord.gateway.dto.ChannelConfigurationTo;
import io.busata.fourleftdiscord.gateway.dto.ClubResultTo;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.ResultEntryTo;
import io.busata.fourleftdiscord.autoposting.domain.AutoPostTracking;
import io.busata.fourleftdiscord.autoposting.domain.AutoPostTrackingRepository;
import io.busata.fourleftdiscord.messages.MessageType;
import io.busata.fourleftdiscord.messages.creation.MessageTemplateFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPostClubResultsService {
    private final FourLeftApi api;
    private final AutoPostTrackingRepository repository;
    private final DiscordMessageFacade discordMessageFacade;
    private final ChannelConfigurationService channelConfigurationService;
    private final MessageTemplateFacade messageTemplateFacade;

    public void update() {
        channelConfigurationService.getChannels()
                .stream()
                .filter(ChannelConfigurationTo::postClubResults)
                .forEach(configuration -> {
                    log.info("Checking for channel {}", configuration.description());
                    final var clubId = configuration.clubId();
                    final var channelId = Snowflake.of(configuration.channelId());

                    try {
                        ClubResultTo currentResults = api.getCurrentResults(clubId);

                        repository.findByEventIdAndChallengeId(currentResults.eventId(), currentResults.eventChallengeId())
                                .ifPresentOrElse((autoPostTracking -> {
                                    handleExistingAutopostEntry(channelId, currentResults, autoPostTracking);
                                }), () -> {
                                    createNewAutopostEntry(currentResults);
                                });
                    } catch (Exception ex) {
                        log.warn("Something wrong while posting auto results, probably has no current event", ex);
                    }
                });
    }

    private void createNewAutopostEntry(ClubResultTo currentResults) {
        log.info("Creating new autopost entry for {}", currentResults.stageName());

        final var autoPostTracking = new AutoPostTracking();

        autoPostTracking.setEventId(currentResults.eventId());
        autoPostTracking.setChallengeId(currentResults.eventChallengeId());
        autoPostTracking.setEntryCount(currentResults.entries().size());
        autoPostTracking.setMemberList(currentResults.entries().stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));

        repository.save(autoPostTracking);
    }

    private void handleExistingAutopostEntry(Snowflake channelId, ClubResultTo currentResults, AutoPostTracking autoPostTracking) {
        log.info("Autopost entry already exists, checking new entries.");

        if (!currentResults.hasNewEntries(autoPostTracking.getEntryCount())) {
            log.info("Entry count still the same, skipping.");
            return;
        }

        final var entryCountDelta = currentResults.sizeEntries() - autoPostTracking.getEntryCount();

        try {
            log.info("Creating and posting message for new entries");

            List<ResultEntryTo> newEntries = getNewEntries(currentResults, autoPostTracking);

            if(entryCountDelta != newEntries.size()) {
                log.info("New entries not the same size as the entry count, something is off, skipping!");
                repository.delete(autoPostTracking);
                return;
            }

            discordMessageFacade.postMessage(
                    channelId,
                    messageTemplateFacade.createAutopostMessage(currentResults, newEntries),
                    MessageType.AUTO_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong while posting the message", ex);
        }

        updateAutopostEntry(currentResults, autoPostTracking);
    }

    private void updateAutopostEntry(ClubResultTo currentResults, AutoPostTracking autoPostTracking) {
        autoPostTracking.setEntryCount(currentResults.entries().size());
        autoPostTracking.setMemberList(currentResults.entries().stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));
        repository.save(autoPostTracking);
    }

    private List<ResultEntryTo> getNewEntries(ClubResultTo currentResults, AutoPostTracking autoPostTracking) {
        List<String> previousEntries = Arrays.asList(autoPostTracking.getMemberList().split(";"));
        return currentResults.entries().stream().filter(entry -> !previousEntries.contains(entry.name())).collect(Collectors.toList());
    }
}
