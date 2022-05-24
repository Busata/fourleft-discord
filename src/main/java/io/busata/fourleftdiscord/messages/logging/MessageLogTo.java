package io.busata.fourleftdiscord.messages.logging;

import io.busata.fourleftdiscord.messages.MessageType;

public record MessageLogTo(MessageType messageType, Long messageId, String author, String content, Long channelId) {
}