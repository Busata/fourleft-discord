package io.busata.fourleftdiscord.commands.configure;


import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class ConfigureCommandFactory implements CommandProvider {

    private final List<BotCommandOptionHandler> commandHandlers;

    @Override
    public ImmutableApplicationCommandRequest create() {
        ImmutableApplicationCommandRequest.Builder configure = ApplicationCommandRequest.builder()
                .name(CommandNames.CONFIGURE)
                .defaultPermission(true)
                .description("Bot configuration");

        commandHandlers.stream()
                .filter(handler -> handler.getCommand().equalsIgnoreCase(CommandNames.CONFIGURE))
                .forEach(handler -> {
                    log.info("Adding handler {}", handler);
                    configure.addOption(handler.buildOption());
                });


        return configure.build();
    }
}
