package io.busata.fourleftdiscord.gateway.dto;

import io.busata.fourleftdiscord.fieldmapper.FieldMappingType;

public record FieldMappingRequestTo(
        String name,
        FieldMappingType type
) {
}
