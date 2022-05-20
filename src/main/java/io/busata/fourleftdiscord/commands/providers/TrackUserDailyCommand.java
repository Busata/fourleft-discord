package io.busata.fourleftdiscord.commands.providers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.channels.ChannelConfigurationService;
import io.busata.fourleftdiscord.commands.WeeklyCommandHandler;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.ChannelConfigurationTo;
import io.busata.fourleftdiscord.gateway.dto.TrackUserRequestTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackUserDailyCommand implements WeeklyCommandHandler {
    private final ChannelConfigurationService channelConfigurationService;

    private final FourLeftApi api;

    @Override
    public String getCommand() {
        return "track";
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getCommand())
                .description("Track user for the daily weekly monthly")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("username")
                        .description("Nickname to be displayed in the results post")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("racenet")
                        .description("Your racenet username (Check if it works on https://dr2.today)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public List<Snowflake> getResponseChannels() {
        return channelConfigurationService.getChannels().stream().filter(ChannelConfigurationTo::postCommunityResults).map(ChannelConfigurationTo::channelId).map(Snowflake::of).toList();
    }

    @Override
    public boolean canHandle(ChatInputInteractionEvent event) {
        return event.getOption(getCommand()).isPresent();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return Mono.just(event).flatMap(evt -> {
            return evt.deferReply().withEphemeral(true).then(trackUser(event));
        });
    }

    private Mono<Void> trackUser(ChatInputInteractionEvent event) {
            String username = event.getOption(getCommand())
                    .flatMap(subCommand -> subCommand.getOption("username"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();
            String racenet = event.getOption(getCommand())
                    .flatMap(subCommand -> subCommand.getOption("racenet"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

            List<String> closestMatches = api.queryUsername(racenet);
            String closestMatch = closestMatches.get(0);


            api.trackUser(new TrackUserRequestTo(username, closestMatch));

            return event.createFollowup(
                    "Registered with **%s** as display name and **%s** as Racenet name.\n*Closest Racenet matches were:\n%s\nIf this is incorrect, Consult https://dr2.today leaderboards to find your correct Racenet nick & register again*\n*Still having issues? Contact @Busata*".formatted(
                            username,
                            closestMatch,
                            String.join("\n", closestMatches)
                    )
            ).withEphemeral(true).then();
    }
}
