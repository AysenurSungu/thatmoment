package com.thatmoment.modules.profile.domain.converter;

import com.thatmoment.modules.profile.domain.enums.Theme;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ThemeConverter implements AttributeConverter<Theme, String> {

    @Override
    public String convertToDatabaseColumn(Theme attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Theme convertToEntityAttribute(String dbData) {
        return Theme.fromValue(dbData);
    }
}
