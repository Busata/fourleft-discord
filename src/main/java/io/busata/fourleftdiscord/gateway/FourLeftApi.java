package io.busata.fourleftdiscord.gateway;

import io.busata.fourleftdiscord.gateway.dto.*;
import io.busata.fourleftdiscord.fieldmapper.FieldMappingTo;
import io.busata.fourleftdiscord.messages.MessageType;
import io.busata.fourleftdiscord.messages.logging.MessageLogTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="fourleftapi", url="https://fourleft.busata.io/api/")
public interface FourLeftApi {

    @GetMapping(value="club/{clubId}/results/current")
    ClubResultTo getCurrentResults(@PathVariable long clubId);

    @GetMapping(value="club/{clubId}/championship_standings")
    List<ChampionshipStandingEntryTo> getStandings(@PathVariable long clubId);

    @GetMapping(value="club/{clubId}/results/previous")
    ClubResultTo getPreviousResults(@PathVariable long clubId);

    @PostMapping(value="community/track_user")
    void trackUser(@RequestBody TrackUserRequestTo request);

    @GetMapping(value="community/results")
    List<CommunityChallengeSummaryTo> getCommunityResults();

    @GetMapping(value="discord/field_mappings")
    List<FieldMappingTo> getFieldMappings();

    @PostMapping(value="discord/field_mappings")
    FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request);

    @GetMapping(value="discord/channels")
    List<ChannelConfigurationTo> getChannels();


    @GetMapping(value="query/track")
    QueryTrackResultsTo queryTrack(@RequestParam String stageName);
    @GetMapping(value="query/name")
    List<String> queryUsername(@RequestParam String query);

    @PostMapping(value="discord/messages")
    void postMessage(@RequestBody MessageLogTo messageLog);

    @GetMapping(value="discord/message")
    boolean hasMessage(@RequestParam long messageId, @RequestParam MessageType messageType);
}
