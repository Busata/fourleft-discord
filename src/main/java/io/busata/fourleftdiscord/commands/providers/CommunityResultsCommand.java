package io.busata.fourleftdiscord.commands.providers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.autoposting.AutoPostCommunityEventResultsService;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
import io.busata.fourleftdiscord.commands.DiscordChannels;
import io.busata.fourleftdiscord.commands.WeeklyCommandHandler;
import io.busata.fourleftdiscord.gateway.dto.ChannelConfigurationTo;
import io.busata.fourleftdiscord.messages.DiscordMessageFacade;
import io.busata.fourleftdiscord.messages.MessageType;
import io.busata.fourleftdiscord.results.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@Order(0)
@RequiredArgsConstructor
public class CommunityResultsCommand implements WeeklyCommandHandler {
    private final AutoPostCommunityEventResultsService autoPostCommunityEventResultsService;

    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getCommand())
                .description("Post community events in the main channel")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .build();
    }
    @Override
    public List<Snowflake> getResponseChannels() {
        return List.of(DiscordChannels.DIRTY_MAIN_CHAT);
    }

    @Override
    public String getCommand() {
        return "commmunity";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().then(createResults(event)).then();
    }

    public Mono<Message> createResults(ChatInputInteractionEvent event) {
        try {
            autoPostCommunityEventResultsService.update();
            return event.createFollowup("Done!").withEphemeral(true);
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong or no active event found? Please try again later!*");
        }
    }
}
