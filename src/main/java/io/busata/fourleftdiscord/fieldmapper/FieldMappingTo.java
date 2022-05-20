package io.busata.fourleftdiscord.fieldmapper;

import java.util.UUID;

public record FieldMappingTo(
        UUID id,
        String name,
        String value,
        FieldMappingType fieldMappingType
) {
}
