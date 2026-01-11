package com.thatmoment.modules.profile.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.thatmoment.modules.profile.domain.enums.Language;
import com.thatmoment.modules.profile.domain.enums.Theme;
import com.thatmoment.modules.profile.domain.enums.TimeFormat;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_preferences", schema = "profile")
public class UserPreferences extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "theme", length = 20)
    private Theme theme;

    @Column(name = "language", length = 10)
    private Language language;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "week_start_day", length = 10)
    private WeekStartDay weekStartDay;

    @Column(name = "time_format", length = 5)
    private TimeFormat timeFormat;

    @Column(name = "notification_routines")
    private Boolean notificationRoutines;

    @Column(name = "notification_achievements")
    private Boolean notificationAchievements;

    @Column(name = "notification_streaks")
    private Boolean notificationStreaks;

    @Column(name = "notification_daily_reminder")
    private Boolean notificationDailyReminder;

    @Column(name = "daily_reminder_time")
    private LocalTime dailyReminderTime;

    @Column(name = "journal_lock_enabled", nullable = false)
    private Boolean journalLockEnabled;

    @Column(name = "journal_password_hash", length = 255)
    private String journalPasswordHash;

    protected UserPreferences() {
    }

    private UserPreferences(Builder builder) {
        this.userId = builder.userId;
        this.theme = builder.theme;
        this.language = builder.language;
        this.timezone = builder.timezone;
        this.weekStartDay = builder.weekStartDay;
        this.timeFormat = builder.timeFormat;
        this.notificationRoutines = builder.notificationRoutines;
        this.notificationAchievements = builder.notificationAchievements;
        this.notificationStreaks = builder.notificationStreaks;
        this.notificationDailyReminder = builder.notificationDailyReminder;
        this.dailyReminderTime = builder.dailyReminderTime;
        this.journalLockEnabled = builder.journalLockEnabled;
        this.journalPasswordHash = builder.journalPasswordHash;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public Theme getTheme() {
        return theme;
    }

    public Language getLanguage() {
        return language;
    }

    public String getTimezone() {
        return timezone;
    }

    public WeekStartDay getWeekStartDay() {
        return weekStartDay;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public Boolean getNotificationRoutines() {
        return notificationRoutines;
    }

    public Boolean getNotificationAchievements() {
        return notificationAchievements;
    }

    public Boolean getNotificationStreaks() {
        return notificationStreaks;
    }

    public Boolean getNotificationDailyReminder() {
        return notificationDailyReminder;
    }

    public LocalTime getDailyReminderTime() {
        return dailyReminderTime;
    }

    public Boolean getJournalLockEnabled() {
        return journalLockEnabled;
    }

    public String getJournalPasswordHash() {
        return journalPasswordHash;
    }

    public void updateDetails(
            Theme theme,
            Language language,
            String timezone,
            WeekStartDay weekStartDay,
            TimeFormat timeFormat,
            Boolean notificationRoutines,
            Boolean notificationAchievements,
            Boolean notificationStreaks,
            Boolean notificationDailyReminder,
            LocalTime dailyReminderTime
    ) {
        this.theme = theme;
        this.language = language;
        this.timezone = timezone;
        this.weekStartDay = weekStartDay;
        this.timeFormat = timeFormat;
        this.notificationRoutines = notificationRoutines;
        this.notificationAchievements = notificationAchievements;
        this.notificationStreaks = notificationStreaks;
        this.notificationDailyReminder = notificationDailyReminder;
        this.dailyReminderTime = dailyReminderTime;
    }

    public void setJournalLock(String passwordHash) {
        this.journalLockEnabled = Boolean.TRUE;
        this.journalPasswordHash = passwordHash;
    }

    public void disableJournalLock() {
        this.journalLockEnabled = Boolean.FALSE;
        this.journalPasswordHash = null;
    }

    public static final class Builder {
        private UUID userId;
        private Theme theme;
        private Language language;
        private String timezone;
        private WeekStartDay weekStartDay;
        private TimeFormat timeFormat;
        private Boolean notificationRoutines;
        private Boolean notificationAchievements;
        private Boolean notificationStreaks;
        private Boolean notificationDailyReminder;
        private LocalTime dailyReminderTime;
        private Boolean journalLockEnabled;
        private String journalPasswordHash;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Builder language(Language language) {
            this.language = language;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder weekStartDay(WeekStartDay weekStartDay) {
            this.weekStartDay = weekStartDay;
            return this;
        }

        public Builder timeFormat(TimeFormat timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }

        public Builder notificationRoutines(Boolean notificationRoutines) {
            this.notificationRoutines = notificationRoutines;
            return this;
        }

        public Builder notificationAchievements(Boolean notificationAchievements) {
            this.notificationAchievements = notificationAchievements;
            return this;
        }

        public Builder notificationStreaks(Boolean notificationStreaks) {
            this.notificationStreaks = notificationStreaks;
            return this;
        }

        public Builder notificationDailyReminder(Boolean notificationDailyReminder) {
            this.notificationDailyReminder = notificationDailyReminder;
            return this;
        }

        public Builder dailyReminderTime(LocalTime dailyReminderTime) {
            this.dailyReminderTime = dailyReminderTime;
            return this;
        }

        public Builder journalLockEnabled(Boolean journalLockEnabled) {
            this.journalLockEnabled = journalLockEnabled;
            return this;
        }

        public Builder journalPasswordHash(String journalPasswordHash) {
            this.journalPasswordHash = journalPasswordHash;
            return this;
        }

        public UserPreferences build() {
            return new UserPreferences(this);
        }
    }
}
