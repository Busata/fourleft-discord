package io.busata.fourleftdiscord.commands.providers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
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
public class CurrentResultsCommand implements WeeklyCommandHandler {
    private final ResultsFetcher resultsFetcher;
    private final ChannelConfigurationService channelConfigurationService;

    private final DiscordMessageFacade discordMessageFacade;

    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getCommand())
                .description("Get results of the active event")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .build();
    }
    @Override
    public List<Snowflake> getResponseChannels() {
        return channelConfigurationService.getChannels().stream().map(ChannelConfigurationTo::channelId).map(Snowflake::of).toList();
    }

    @Override
    public String getCommand() {
        return "current";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().then(createResults(event)).doOnNext(message -> {
            discordMessageFacade.logMessage(message, MessageType.RESULTS_POST);
        }).then();
    }

    public Mono<Message> createResults(ChatInputInteractionEvent event) {
        try {
            return event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(resultsFetcher.getWeeklyResults(event.getInteraction().getChannelId())).build());
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong or no active event found? Please try again later!*");
        }
    }
}
