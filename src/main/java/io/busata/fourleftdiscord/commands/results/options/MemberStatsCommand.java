package io.busata.fourleftdiscord.commands.results.options;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
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
@Order(3)
@RequiredArgsConstructor
public class MemberStatsCommand implements BotCommandOptionHandler {

    private final ResultsFetcher resultsFetcher;
    private final ChannelConfigurationService channelConfigurationService;
    private final DiscordMessageFacade discordMessageFacade;

    @Override
    public String getCommand() {
        return CommandNames.RESULTS;
    }
    @Override
    public String getOption() {
        return CommandOptions.STATS;
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Get statistics for the club members")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .build();
    }


    @Override
    public List<Snowflake> getResponseChannels() {
        return channelConfigurationService.getChannels().stream().map(ChannelConfigurationTo::channelId).map(Snowflake::of).toList();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().then(createStats(event)).doOnNext(message -> {
            discordMessageFacade.logMessage(message, MessageType.RESULTS_POST);
        }).then();
    }

    public Mono<Message> createStats(ChatInputInteractionEvent event) {
        try {
            return event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(resultsFetcher.getClubMemberStatsMessage(event.getInteraction().getChannelId())).build());
        } catch (Exception ex) {
            log.error("Error while loading the results", ex);
            return event.createFollowup("*Something went wrong. Please try again later!*");
        }
    }
}
