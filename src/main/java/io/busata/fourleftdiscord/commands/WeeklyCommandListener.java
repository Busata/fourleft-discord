package io.busata.fourleftdiscord.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeeklyCommandListener implements EventListener<ChatInputInteractionEvent> {
    private final GatewayDiscordClient client;

    private final List<WeeklyCommandHandler> commandHandlers;

    @PostConstruct
    public void createCommand() {

        long applicationId = client.getRestClient().getApplicationId().block();

        ImmutableApplicationCommandRequest resultsCommand = buildCommand();


        client.getChannelById(Snowflake.of(817405818349682729L)).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(Snowflake.of(978949777410646086L)).flatMap(Message::delete);
        }).block();
        client.getChannelById(Snowflake.of(817405818349682729L)).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(Snowflake.of(978933990717280316L)).flatMap(Message::delete);
        }).block();

        client.getChannelById(Snowflake.of(817405818349682729L)).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(Snowflake.of(978874762937897010L)).flatMap(Message::delete);
        }).block();
        client.getChannelById(Snowflake.of(817405818349682729L)).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(Snowflake.of(978814365530730556L)).flatMap(Message::delete);
        }).block();
        
        client.getChannelById(Snowflake.of(817405818349682729L)).ofType(MessageChannel.class).flatMap(channel -> {
            return channel.getMessageById(Snowflake.of(978411268987813928L)).flatMap(Message::delete);
        }).block();

        List.of(DiscordGuilds.DIRTY_DISCORD, DiscordGuilds.BUSATA_DISCORD, DiscordGuilds.GRF_DISCORD).forEach(guild -> {
            List<String> discordCommands = client.getRestClient()
                    .getApplicationService()
                    .getGuildApplicationCommands(applicationId, guild)
                    .map(ApplicationCommandData::id)
                    .collectList().block();

            discordCommands.forEach(commandId -> {
                client.getRestClient().getApplicationService().deleteGuildApplicationCommand(applicationId, guild, Long.parseLong(commandId)).subscribe();
            });
        });

        List.of(DiscordGuilds.DIRTY_DISCORD, DiscordGuilds.BUSATA_DISCORD, DiscordGuilds.GRF_DISCORD).forEach(guild -> {
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, guild, resultsCommand)
                    .subscribe();
        });
    }

    private ImmutableApplicationCommandRequest buildCommand() {
        ImmutableApplicationCommandRequest.Builder weekly = ApplicationCommandRequest.builder()
                .name("results")
                .description("Get the results of the current weekly event");

        for (WeeklyCommandHandler commandHandler : commandHandlers) {
            log.info("Adding handler {}", commandHandler);
            weekly.addOption(commandHandler.buildOption());
        }

        return weekly.build();
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        List<Mono<Void>> results = commandHandlers.stream().map(handler -> Mono.just(event)
                .filter(evt -> evt.getCommandName().equals("results"))
                .filter(handler::canHandle)
                .flatMap(evt ->
                        evt.getInteraction()
                                .getChannel()
                                .flatMap(channel -> {
                                    if(handler.canRespond(channel.getId())) {
                                        return handler.handle(evt, channel);
                                    } else {
                                        return event.reply("Sorry, I'm not listening to this command in this channel.").withEphemeral(true).then();
                                    }
                                }))).collect(Collectors.toList());

        return Flux.fromIterable(results).flatMap(x -> x).collectList().then();
    }
}
