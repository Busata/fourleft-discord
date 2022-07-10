package io.busata.fourleftdiscord.gateway.dto;

public record ClubMemberTo(
        String displayName,
        String membershipType,
        long championshipGolds,
        long championshipSilvers,
        long championshipBronzes,
        long championshipParticipation
) {
}