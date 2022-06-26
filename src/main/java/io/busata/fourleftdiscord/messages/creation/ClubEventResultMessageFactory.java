package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleftdiscord.gateway.dto.ClubResultTo;
import io.busata.fourleftdiscord.gateway.dto.ResultEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClubEventResultMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public EmbedCreateSpec create(ClubResultTo clubResult) {
        final var builder = EmbedCreateSpec.builder();
        builder.title("**Results**");
        builder.color(Color.of(244, 0, 75));
        builder.thumbnail(fieldMapper.createImage(clubResult.country()));

        builder.addField("Country", "%s".formatted(fieldMapper.createEmoticon(clubResult.country())), true);
        builder.addField( clubResult.stageNames().size() > 0 ? "Stages" : "Stage", String.join(" • ", clubResult.stageNames()), true);
        builder.addField("Car", fieldMapper.createHumanReadable(clubResult.vehicleClass()), true);

        final var sortedEntries = clubResult.entries().stream().sorted(Comparator.comparing(ResultEntryTo::rank)).limit(50).collect(Collectors.toList());


        final var fieldsRequired = (int) Math.ceil(sortedEntries.size() / 10f);

        for (int i = 0; i < fieldsRequired; i++) {
            String collect = sortedEntries.stream().skip(i * 10L).limit(10).map(entry -> {
                return String.format("**%s** • **%s** • **%s** • %s *(%s)*", entry.rank(), fieldMapper.createEmoticon(entry.nationality()), entry.name(), entry.stageTime(), entry.stageDiff());
            }).collect(Collectors.joining("\n"));

            builder.addField(determineHeader(i), collect, false);
        }

        if(clubResult.entries().size() > 50) {
            builder.addField("\u200B", "*Results limited to     top 50 (Total: %s entries)*".formatted(clubResult.entries().size()), false);
        }


        builder.addField("**Last update**", "*%s*".formatted(new PrettyTime().format(clubResult.lastUpdate())), true);
        builder.addField("**Event ending**", "<t:%s:R>".formatted(clubResult.endTime().toInstant().atZone(ZoneOffset.UTC).toEpochSecond()), true);

        return builder.build();
    }

    private String determineHeader(int idx) {
        if (idx == 0) {
            return "Top 10";
        } else {
            var startBound = (idx * 10) + 1;
            var endBound = (idx * 10) + 10;

            return "Top %s - %s".formatted(startBound, endBound);
        }
    }
}
