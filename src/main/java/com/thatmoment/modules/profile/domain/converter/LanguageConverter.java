package com.thatmoment.modules.profile.domain.converter;

import com.thatmoment.modules.profile.domain.enums.Language;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<Language, String> {

    @Override
    public String convertToDatabaseColumn(Language attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Language convertToEntityAttribute(String dbData) {
        return Language.fromValue(dbData);
    }
}
