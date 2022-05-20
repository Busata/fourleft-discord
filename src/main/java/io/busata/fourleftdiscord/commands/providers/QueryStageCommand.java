package io.busata.fourleftdiscord.commands.providers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import io.busata.fourleftdiscord.commands.WeeklyCommandHandler;
import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.QueryTrackResultsTo;
import io.busata.fourleftdiscord.messages.creation.QueryMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueryStageCommand implements WeeklyCommandHandler {

    private final QueryMessageFactory queryMessageFactory;
    private final FourLeftApi api;

    @Override
    public String getCommand() {
        return "query";
    }
    @Override
    public ImmutableApplicationCommandOptionData buildOption() {
        return ApplicationCommandOptionData.builder()
                .name(getCommand())
                .description("Query stage name")
                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("stage")
                        .description("Part of a stage name")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel) {
        return event.deferReply().withEphemeral(true).then(queryStage(event));
    }

    public Mono<Void> queryStage(ChatInputInteractionEvent event) {
        return Mono.just(event).flatMap(evt -> {

            String stageName = event.getOption(getCommand())
                    .flatMap(subCommand -> subCommand.getOption("stage"))
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

            try {
                QueryTrackResultsTo queryTrackResultsTo = api.queryTrack(stageName);
                EmbedCreateSpec embedCreateSpec = queryMessageFactory.create(queryTrackResultsTo);
                return evt.createFollowup(InteractionFollowupCreateSpec.builder().addEmbed(embedCreateSpec).build().withEphemeral(true)).then();
            } catch (Exception ex) {
                log.info("Something went wrong, input: {}", stageName, ex);
                return evt.createFollowup("Could not find any stage with this query").withEphemeral(true).then();
            }

        });
    }
}
