package com.thatmoment.modules.profile.domain.converter;

import com.thatmoment.modules.profile.domain.enums.TimeFormat;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TimeFormatConverter implements AttributeConverter<TimeFormat, String> {

    @Override
    public String convertToDatabaseColumn(TimeFormat attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TimeFormat convertToEntityAttribute(String dbData) {
        return TimeFormat.fromValue(dbData);
    }
}
