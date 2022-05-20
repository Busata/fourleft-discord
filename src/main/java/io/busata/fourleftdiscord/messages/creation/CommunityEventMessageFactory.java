package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleftdiscord.gateway.dto.CommunityChallengeBoardEntryTo;
import io.busata.fourleftdiscord.gateway.dto.CommunityChallengeSummaryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CommunityEventMessageFactory {
    private final DR2FieldMapper fieldMapper;

    private List<Color> embedColours = List.of(
            Color.of(244, 0, 75),
            Color.ORANGE,
            Color.JAZZBERRY_JAM,
            Color.GREEN,
            Color.RED,
            Color.MAGENTA,
            Color.TAHITI_GOLD
    );

    public List<EmbedCreateSpec> getEmbeds(List<CommunityChallengeSummaryTo> communityResults) {

        List<EmbedCreateSpec.Builder> dailies = communityResults
                .stream()
                .filter(CommunityChallengeSummaryTo::isDaily)
                .filter(CommunityChallengeSummaryTo::hasEntries)
                .map(this::createEmbed)
                .collect(Collectors.toList());

        List<EmbedCreateSpec.Builder> weeklies = communityResults
                .stream()
                .filter(CommunityChallengeSummaryTo::isWeekly)
                .filter(CommunityChallengeSummaryTo::hasEntries)
                .map(this::createEmbed).collect(Collectors.toList());

        List<EmbedCreateSpec.Builder> monthlies = communityResults
                .stream()
                .filter(CommunityChallengeSummaryTo::isMonthly)
                .filter(CommunityChallengeSummaryTo::hasEntries)
                .map(this::createEmbed).collect(Collectors.toList());

        final var infoMessage = List.of(EmbedCreateSpec.builder().footer("Add your name to the board? /results track", null));

        List<EmbedCreateSpec.Builder> collect = Stream.of(dailies, weeklies, monthlies, infoMessage)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (int i = 0; i < collect.size(); i++) {
            collect.get(i).color(embedColours.get(i));
        }

        return collect.stream().map(EmbedCreateSpec.Builder::build).collect(Collectors.toList());
    }

    private EmbedCreateSpec.Builder createEmbed(CommunityChallengeSummaryTo communityChallenge) {
        final var builder = EmbedCreateSpec.builder();
        CommunityChallengeBoardEntryTo firstEntry = communityChallenge.firstEntry();

        builder.title("**%s Challenge** • %s • **%s** • **%s**".formatted(
                communityChallenge.type().name(),
                communityChallenge.eventLocations().stream().map(fieldMapper::createEmoticon).collect(Collectors.joining(" ")),
                communityChallenge.firstStageName(),
                fieldMapper.createHumanReadable(communityChallenge.vehicleClass())
        ));

        builder.description("**Fastest driver**:\n :first_place: • %s • **%s** • *%s*\n<:blank:894976571406966814>".formatted(
                fieldMapper.createEmoticon(firstEntry.nationality()),
                firstEntry.name(),
                firstEntry.stageTime()
        ));


        final var sortedEntries = communityChallenge.entries();

        final var fieldsRequired = (int) Math.ceil(sortedEntries.size() / 10f);


        for (int i = 0; i < fieldsRequired; i++) {
            String collect = sortedEntries.stream().skip(i * 10L).limit(10).map(boardEntry -> {
                return String.format("%s • %s • **%s** • **%s** • *(%s)*",
                        createBadge(boardEntry),
                        fieldMapper.createEmoticon(boardEntry.nationality()),
                        boardEntry.name(),
                        ordinal((int) boardEntry.rank()),
                        boardEntry.stageDiff()
                );
            }).collect(Collectors.joining("\n"));

            builder.addField(determineHeader(i), collect, false);
        }

        builder.footer("Number of entries (global): %s".formatted(firstEntry.totalRank()), null);

        return builder;
    }

    private String determineHeader(int i) {
        if (i == 0) {
            return "DiRTy Gossip Drivers:";
        } else {
            return "\u200B";
        }
    }

    public String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    private String createBadge(CommunityChallengeBoardEntryTo entry) {
        final var badge = createBadgeIcon(entry);
        return String.format("%s **Top %s%%**", badge, (int) Math.ceil(entry.percentageRank()));
    }

    private String createBadgeIcon(CommunityChallengeBoardEntryTo entry) {
        final var percentageRank = entry.percentageRank();
        if (entry.isDnf()) {
            return "<:f_respects:894913859532496916>";
        } else if (percentageRank <= 1) {
            return "<:Rank_S:971454722030600214>";
        } else if (percentageRank <= 10) {
            return "<:Rank_A:971454722458411048>";
        } else if (percentageRank <= 35) {
            return "<:Rank_B:971454722429046824>";
        } else if (percentageRank <= 75) {
            return "<:Rank_C:971454722043150387>";
        } else {
            return "<:Rank_D:971454722244497410>";
        }
    }
}
