package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.gateway.dto.ChampionshipStandingEntryTo;
import io.busata.fourleftdiscord.gateway.dto.ClubMemberTo;
import io.busata.fourleftdiscord.gateway.dto.ClubResultTo;
import io.busata.fourleftdiscord.gateway.dto.CommunityChallengeSummaryTo;
import io.busata.fourleftdiscord.gateway.dto.ResultEntryTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageTemplateFacade {
    private final ChampionshipStandingsMessageFactory championshipStandingsMessageFactory;
    private final ClubEventResultMessageFactory clubEventResultMessageFactory;
    private final ClubMembersMessageFactory clubMembersMessageFactory;
    private final CommunityEventMessageFactory communityEventMessageFactory;

    private final AutoPostResultMessageFactory autoPostResultMessageFactory;

    public EmbedCreateSpec createEmbedFromStandingEntries(List<ChampionshipStandingEntryTo> result) {
        return championshipStandingsMessageFactory.create(result);
    }

    public EmbedCreateSpec createEmbedFromClubResult(ClubResultTo result) {
        return clubEventResultMessageFactory.create(result);
    }

    public List<EmbedCreateSpec> createEmbedFromCommunityEventResults(List<CommunityChallengeSummaryTo> events) {
        return communityEventMessageFactory.getEmbeds(events);
    }

    public String createAutopostMessage(ClubResultTo clubResultTo, List<ResultEntryTo> entries) {
        return autoPostResultMessageFactory.create(clubResultTo, entries);
    }


    public EmbedCreateSpec createEmbedFromMembers(List<ClubMemberTo> members) {
        return clubMembersMessageFactory.create(members);
    }
}
