package io.busata.fourleftdiscord.autoposting;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.possible.Possible;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        log.warn("!Something wrong while posting auto results, probably has no current event", ex);
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
        autoPostTracking.setLastPostedMembers("");

        repository.save(autoPostTracking);
    }

    private void handleExistingAutopostEntry(Snowflake channelId, ClubResultTo currentResults, AutoPostTracking autoPostTracking) {
        log.info("** Autopost entry already exists, checking new entries.");

        if (!currentResults.hasNewEntries(autoPostTracking.getEntryCount())) {
            log.info("**** Entry count still the same, skipping.");
            return;
        }

        log.info("** Creating and posting message for new entries");
        final var entryCountDelta = currentResults.sizeEntries() - autoPostTracking.getEntryCount();

        List<ResultEntryTo> newEntries = getNewEntries(currentResults, autoPostTracking);

        if (entryCountDelta != newEntries.size()) {
            log.info("!! Entry list size change is different than actual entry count, something is off, skipping!");
            repository.delete(autoPostTracking);
            return;
        }

        final var lastMessage = discordMessageFacade.getLastMessage(channelId);

        if (!autoPostTracking.getLastPostedMembers().isBlank() && api.hasMessage(lastMessage.getId().asLong(), MessageType.AUTO_POST)) {
            log.info("**** Editing previous message");
            //Edit last message instead
            editPreviousMessage(lastMessage, currentResults, newEntries, autoPostTracking);
        } else {
            log. info("**** Posting new message");
            // post new message
            postNewMessage(channelId, currentResults, newEntries, autoPostTracking);
        }
    }

    private void postNewMessage(Snowflake channelId, ClubResultTo currentResults, List<ResultEntryTo> newEntries, AutoPostTracking autoPostTracking) {
        final var autopostMessage = messageTemplateFacade.createAutopostMessage(currentResults, newEntries);

        discordMessageFacade.postMessage(
                channelId,
                autopostMessage,
                MessageType.AUTO_POST
        );

        autoPostTracking.setEntryCount(currentResults.entries().size());
        autoPostTracking.setMemberList(currentResults.entries().stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));
        autoPostTracking.setLastPostedMembers(newEntries.stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));
        repository.save(autoPostTracking);

    }

    private void editPreviousMessage(Message lastMessage, ClubResultTo currentResults, List<ResultEntryTo> newEntries, AutoPostTracking autoPostTracking) {
        List<String> previousEntries = Arrays.asList(autoPostTracking.getLastPostedMembers().split(";"));

        final var extraEntries = currentResults.entries().stream().filter(entry -> previousEntries.contains(entry.name())).toList();

        final var entriesToPost = Stream.concat(extraEntries.stream(), newEntries.stream()).sorted(Comparator.comparing(ResultEntryTo::rank)).collect(Collectors.toList());

        final var autopostMessage = messageTemplateFacade.createAutopostMessage(currentResults, entriesToPost);

        final var possibleMesage = Possible.of(java.util.Optional.ofNullable(autopostMessage));

        lastMessage.edit(MessageEditSpec.builder().content(possibleMesage).build()).block();

        autoPostTracking.setEntryCount(currentResults.entries().size());
        autoPostTracking.setMemberList(currentResults.entries().stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));
        autoPostTracking.setLastPostedMembers(entriesToPost.stream().map(ResultEntryTo::name).collect(Collectors.joining(";")));
        repository.save(autoPostTracking);
    }

    private List<ResultEntryTo> getNewEntries(ClubResultTo currentResults, AutoPostTracking autoPostTracking) {
        List<String> previousEntries = Arrays.asList(autoPostTracking.getMemberList().split(";"));
        return currentResults.entries().stream().filter(entry -> !previousEntries.contains(entry.name())).collect(Collectors.toList());
    }
}
