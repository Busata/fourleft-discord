package io.busata.fourleftdiscord.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WeeklyCommandHandler {
    ImmutableApplicationCommandOptionData buildOption();

    default boolean canRespond(Snowflake channelId) {
        final var responseChannels = getResponseChannels();
        return responseChannels.size() == 0 || responseChannels.contains(channelId);
    }

    default List<Snowflake> getResponseChannels() {
        return List.of();
    }

    String getCommand();

    default boolean canHandle(ChatInputInteractionEvent event) {
        return event.getOption(getCommand()).isPresent();
    }

    Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel);
}
