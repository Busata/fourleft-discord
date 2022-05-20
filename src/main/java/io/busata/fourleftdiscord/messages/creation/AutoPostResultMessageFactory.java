package io.busata.fourleftdiscord.messages.creation;

import io.busata.fourleftdiscord.gateway.dto.ClubResultTo;
import io.busata.fourleftdiscord.gateway.dto.ResultEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AutoPostResultMessageFactory {
    private final DR2FieldMapper fieldMapper;

    public String create(ClubResultTo currentResults, List<ResultEntryTo> newEntries) {
        return String.format("**Results** • **%s** • **%s** • **%s**\n%s",
                fieldMapper.createEmoticon(currentResults.country()),
                currentResults.stageName(),
                fieldMapper.createHumanReadable(currentResults.vehicleClass()),
                newEntries.stream()
                        .map(entry -> String.format(":new: • **%s** • **%s** • **%s** • %s *(%s)*",
                                entry.rank(),
                                fieldMapper.createEmoticon(entry.nationality()),
                                entry.name(),
                                entry.stageTime(),
                                entry.stageDiff()))
                        .collect(Collectors.joining("\n"))
        );
    }
}
