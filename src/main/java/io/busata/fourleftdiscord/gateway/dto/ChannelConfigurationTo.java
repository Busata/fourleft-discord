package io.busata.fourleftdiscord.gateway.dto;

public record ChannelConfigurationTo(String description, long channelId, Long clubId, boolean postClubResults, boolean postCommunityResults) {

}
