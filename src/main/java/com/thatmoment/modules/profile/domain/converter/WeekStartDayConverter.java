package com.thatmoment.modules.profile.domain.converter;

import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WeekStartDayConverter implements AttributeConverter<WeekStartDay, String> {

    @Override
    public String convertToDatabaseColumn(WeekStartDay attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public WeekStartDay convertToEntityAttribute(String dbData) {
        return WeekStartDay.fromValue(dbData);
    }
}
