package io.busata.fourleftdiscord.channels;

import discord4j.common.util.Snowflake;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.ChannelConfigurationTo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelConfigurationService {
    private final FourLeftApi api;

    @Cacheable("channel_configurations")
    public List<ChannelConfigurationTo> getChannels() {
        return this.api.getChannels();
    }

    public Long findClubByChannelId(Snowflake channelId) {
        final var channelConfiguration = this.getChannels().stream().filter(c -> channelId.asLong() == c.channelId()).findFirst().orElseThrow();
        return channelConfiguration.clubId();
    }
}
