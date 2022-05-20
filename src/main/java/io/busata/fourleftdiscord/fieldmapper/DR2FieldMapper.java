package io.busata.fourleftdiscord.fieldmapper;

import io.busata.fourleftdiscord.gateway.FourLeftApi;
import io.busata.fourleftdiscord.gateway.dto.FieldMappingRequestTo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DR2FieldMapper {
    private final FourLeftApi fourLeftApi;

    public String createEmoticon(String value) {
        return getFieldMapping(value, FieldMappingType.EMOTE);
    }

    public String createHumanReadable(String value) {
        return getFieldMapping(value, FieldMappingType.HUMAN_READABLE);
    }

    public String createImage(String value) {
        return getFieldMapping(value, FieldMappingType.IMAGE);
    }

    private String getFieldMapping(String name, FieldMappingType type) {
        return getFieldMappings().stream()
                .filter(fieldMappingTo -> fieldMappingTo.name().equals(name) && fieldMappingTo.fieldMappingType() == type)
                .findFirst()
                .map(FieldMappingTo::value)
                .orElseGet(() -> this.saveMissingMapping(name, type).value());
    }

    private FieldMappingTo saveMissingMapping(String name, FieldMappingType type) {
        return this.fourLeftApi.createFieldMapping(new FieldMappingRequestTo(name, type));
    }

    @Cacheable("field_mapppings")
    public List<FieldMappingTo> getFieldMappings() {
        return this.fourLeftApi.getFieldMappings();
    }

}
