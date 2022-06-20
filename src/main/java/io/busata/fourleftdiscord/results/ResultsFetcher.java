package io.busata.fourleftdiscord.results;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
import io.busata.fourleftdiscord.gateway.*;
import io.busata.fourleftdiscord.gateway.dto.CommunityChallengeSummaryTo;
import io.busata.fourleftdiscord.messages.creation.MessageTemplateFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResultsFetcher {
    private final MessageTemplateFacade messageTemplateFacade;
    private final FourLeftApi api;

    private final ChannelConfigurationService channelConfigurationService;

    public EmbedCreateSpec getCurrentEventResults(Snowflake channelId) {
        final var clubId = channelConfigurationService.findClubByChannelId(channelId);
        final var clubResult = api.getCurrentResults(clubId);
        return messageTemplateFacade.createEmbedFromClubResult(clubResult);
    }

    public EmbedCreateSpec getPreviousEventResults(Snowflake channelId) {
        final var clubId = channelConfigurationService.findClubByChannelId(channelId);

        final var clubResult = api.getPreviousResults(clubId);
        return messageTemplateFacade.createEmbedFromClubResult(clubResult);
    }

    public EmbedCreateSpec getChampionshipStandingsMessage(Snowflake channelId) {
        final var clubId = channelConfigurationService.findClubByChannelId(channelId);

        final var result = api.getStandings(clubId);
        return messageTemplateFacade.createEmbedFromStandingEntries(result);
    }
    public List<EmbedCreateSpec> getCommunityEventMessages() {
        List<CommunityChallengeSummaryTo> communityResults = api.getCommunityResults();
        return messageTemplateFacade.createEmbedFromCommunityEventResults(communityResults);
    }




}
