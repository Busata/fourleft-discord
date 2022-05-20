package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleftdiscord.gateway.dto.ChampionshipStandingEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChampionshipStandingsMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(List<ChampionshipStandingEntryTo> result) {
        final var builder = EmbedCreateSpec.builder();
        builder.title("**Championship standings**");
        builder.color(Color.of(244, 0, 75));

        final var sortedEntries = result.stream()
                .sorted(Comparator.comparing(ChampionshipStandingEntryTo::rank))
                .limit(30).collect(Collectors.toList());

        final var fieldsRequired = (int) Math.ceil(sortedEntries.size() / 10f);

        for (int i = 0; i < fieldsRequired; i++) {
            String collect = sortedEntries.stream().skip(i * (i==1 ? 12L : 10L)).limit(i==0 ? 12: i== 1 ? 8: 10).map(entry -> {
                return String.format("**%s** • %s • **%s** • %s",
                        entry.rank(),
                        fieldMapper.createEmoticon(entry.nationality()),
                        entry.displayName(),
                        entry.points()
                );
            }).collect(Collectors.joining("\n"));

            builder.addField(determineStandingsHeader(i), collect, false);
        }

        return builder.build();
    }

    private String determineStandingsHeader(int i) {
        if (i == 0) {
            return "Entries";
        } else {
            return "\u200B";
        }
    }
}
