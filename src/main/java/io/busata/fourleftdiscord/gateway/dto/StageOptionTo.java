package io.busata.fourleftdiscord.gateway.dto;

public record StageOptionTo(
        String displayName,
        Double length,
        boolean isQueried
) {
}
