package io.busata.fourleftdiscord.gateway.dto;

public record ChampionshipStandingEntryTo(
        Long rank,
        String nationality,
        String displayName,
        Long points
) {
}