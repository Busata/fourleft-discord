package io.busata.fourleftdiscord.autoposting;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.commands.DiscordChannels;
import io.busata.fourleftdiscord.messages.DiscordMessageFacade;
import io.busata.fourleftdiscord.messages.MessageType;
import io.busata.fourleftdiscord.results.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPostCommunityEventResultsService
{
    private final ResultsFetcher resultsFetcher;
    private final DiscordMessageFacade discordMessageFacade;

    public void update() {
        List<EmbedCreateSpec> messages = resultsFetcher.getCommunityEventMessages();

        messages.forEach(message -> {
            try {
                discordMessageFacade.postMessage(
                        DiscordChannels.DIRTY_MAIN_CHAT,
                        message,
                        MessageType.COMMUNITY_EVENT
                );
                Thread.sleep(1000);
            } catch (Exception ex) {
                log.error("Something went wrong posting the daily results", ex);
            }
        });
    }


}
