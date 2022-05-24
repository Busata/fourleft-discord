package io.busata.fourleftdiscord.autoposting;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.commands.DiscordChannels;
import io.busata.fourleftdiscord.messages.DiscordMessageFacade;
import io.busata.fourleftdiscord.messages.MessageType;
import io.busata.fourleftdiscord.results.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPosterAutomatedClubService {
    private final ResultsFetcher resultsFetcher;
    private final DiscordMessageFacade discordUtils;
    Snowflake clubId = Snowflake.of(418341);

    public void postResults() {
        EmbedCreateSpec message = resultsFetcher.getPreviousEventResults(DiscordChannels.DIRTY_DAILIES);


        try {
            discordUtils.postMessage(
                    DiscordChannels.DIRTY_DAILIES,
                    message.withTitle("Daily results"),
                            MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the weekly results", ex);
        }
    }

    public void postNewStage() {
        EmbedCreateSpec message = resultsFetcher.getCurrentEventResults(DiscordChannels.DIRTY_DAILIES);

            try {
                discordUtils.postMessage(
                        DiscordChannels.DIRTY_DAILIES,
                        message.withTitle("New Daily Challenge"),
                        MessageType.AUTOMATED_CLUB_POST
                );
            } catch (Exception ex) {
                log.error("Something went wrong posting the weekly results", ex);
            }
    }
}


