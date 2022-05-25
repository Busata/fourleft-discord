package io.busata.fourleftdiscord.commands.configure.options;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandOptions;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.ChannelConfigurationTo;
import io.busata.fourleftdiscord.gateway.dto.QueryTrackResultsTo;
import io.busata.fourleftdiscord.messages.creation.ConfigurationMessageFactory;
import io.busata.fourleftdiscord.messages.creation.QueryMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConfigureChannelCommand implements BotCommandOptionHandler {

    private final FourLeftApi api;
    private final ConfigurationMessageFactory configurationMessageFactory;

    @Override
    public String getCommand() {
        return CommandNames.CONFIGURE;
    }
    @Override
    public String getOption() {
        return CommandOptions.CHANNEL;
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getOption())
                .description("Configure channel")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .build();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().withEphemeral(true).then(logSummary(event)).then();
    }

    private Mono<Void> logSummary(ChatInputInteractionEvent event) {
        return Mono.just(event)
                .flatMap(evt -> {
                    final var channelConfigurations = api.getChannels();

                   return channelConfigurations.stream().filter(config -> {
                        return config.channelId() == event.getInteraction().getChannelId().asLong();
                    }).findFirst()
                            .map(configuration -> {
                                return event.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(configurationMessageFactory.create(configuration)).build());
                            }).orElseGet(() -> {
                                return event.createFollowup("This channel has no configuration");
                            });
                }).then();
    }
}
