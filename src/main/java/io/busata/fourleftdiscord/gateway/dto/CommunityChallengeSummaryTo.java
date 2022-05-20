package io.busata.fourleftdiscord.gateway.dto;

import java.util.List;

public record CommunityChallengeSummaryTo(
        DR2CommunityEventType type,
        String vehicleClass,
        List<String> eventLocations,
        String firstStageName,
        CommunityChallengeBoardEntryTo firstEntry,
        List<CommunityChallengeBoardEntryTo> entries
) {

    public boolean isDaily() {
        return type == DR2CommunityEventType.Daily;
    }

    public boolean isWeekly() {
        return type == DR2CommunityEventType.Weekly;
    }

    public boolean isMonthly() {
        return type == DR2CommunityEventType.Monthly;
    }

    public boolean hasEntries() {
        return this.entries.size() > 0;
    }
}