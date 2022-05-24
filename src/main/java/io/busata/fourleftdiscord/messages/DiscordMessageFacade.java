package io.busata.fourleftdiscord.messages;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.messages.logging.MessageLogTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordMessageFacade {
    private final GatewayDiscordClient client;
    private final FourLeftApi api;

    public void postMessage(Snowflake channelId, String entryMessage, MessageType messageType) {
       final var message = client.getChannelById(channelId)
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(entryMessage)).block();
       logMessage(message, messageType);
    }
    public void postMessage(Snowflake channelId, EmbedCreateSpec embed, MessageType messageType) {
        final var message = client.getChannelById(channelId)
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(embed)).block();

        logMessage(message, messageType);
    }

    public void logMessage(Message message, MessageType messageType) {
        api.postMessage(new MessageLogTo(
                messageType,
                message.getId().asLong(),
                message.getAuthor().map(User::getUsername).orElse(""),
                message.getContent(),
                message.getChannelId().asLong()));
    }

    public Message getLastMessage(Snowflake channelId) {
        return client.getChannelById(channelId).ofType(MessageChannel.class).flatMap(MessageChannel::getLastMessage).block();
    }
}
