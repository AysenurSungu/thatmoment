# That Moment – Project Structure

```text
that-moment-backend/
├── README.md
├── .gitignore
├── .editorconfig
├── pom.xml
├── docker/
│   ├── docker-compose.dev.yml
│   ├── docker-compose.test.yml
│   ├── postgres/
│   │   ├── init.sql
│   │   └── README.md
│   ├── redis/
│   │   └── redis.conf
│   └── localstack/
│       ├── init-aws.sh
│       └── README.md
├── scripts/
│   ├── dev-up.sh
│   ├── dev-down.sh
│   ├── test-up.sh
│   ├── test-down.sh
│   └── wait-for-it.sh
├── docs/
│   ├── project_overview.md
│   ├── api-guidelines.md
│   ├── architecture.md
│   ├── decisions/
│   │   ├── ADR-0001-modular-monolith.md
│   │   ├── ADR-0002-error-format-problem-details.md
│   │   ├── ADR-0003-auth-jwt-refresh-device-session.md
│   │   └── ADR-0004-idempotency-key.md
│   └── postman/
│       └── thatmoment.postman_collection.json
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── thatmoment/
│   │   │           ├── ThatMomentApplication.java
│   │   │           ├── common/
│   │   │           │   ├── config/
│   │   │           │   │   ├── JpaAuditingConfig.java
│   │   │           │   │   ├── JacksonConfig.java
│   │   │           │   │   ├── ClockConfig.java
│   │   │           │   │   ├── WebConfig.java
│   │   │           │   │   └── OpenApiConfig.java
│   │   │           │   ├── entity/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   ├── SoftDeletableEntity.java
│   │   │           │   │   ├── VersionedBaseEntity.java
│   │   │           │   │   └── VersionedSoftDeletableEntity.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── SoftDeleteRepository.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── ProblemDetailsFactory.java
│   │   │           │   │   ├── ErrorCode.java
│   │   │           │   │   ├── ApiException.java
│   │   │           │   │   └── exceptions/
│   │   │           │   │       ├── NotFoundException.java
│   │   │           │   │       ├── ConflictException.java
│   │   │           │   │       ├── ValidationException.java
│   │   │           │   │       └── UnauthorizedException.java
│   │   │           │   ├── web/
│   │   │           │   │   ├── filter/
│   │   │           │   │   │   ├── TraceIdFilter.java
│   │   │           │   │   │   ├── RequestLoggingFilter.java
│   │   │           │   │   │   └── MobileHeadersFilter.java
│   │   │           │   │   ├── response/
│   │   │           │   │   │   ├── ApiResponse.java
│   │   │           │   │   │   └── PageResponse.java
│   │   │           │   │   └── util/
│   │   │           │   │       └── IpExtractor.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── CurrentUser.java
│   │   │           │   │   ├── CurrentUserResolver.java
│   │   │           │   │   └── SecurityConstants.java
│   │   │           │   ├── validation/
│   │   │           │   │   ├── ValidationGroups.java
│   │   │           │   │   └── annotations/
│   │   │           │   │       └── EnumValue.java
│   │   │           │   ├── annotation/
│   │   │           │   │   ├── RateLimit.java
│   │   │           │   │   └── Idempotent.java
│   │   │           │   └── util/
│   │   │           │       ├── HashingUtils.java
│   │   │           │       ├── IdGenerator.java
│   │   │           │       └── TimeUtils.java
│   │   │           ├── auth/
│   │   │           │   ├── api/
│   │   │           │   │   ├── AuthController.java
│   │   │           │   │   └── OAuthController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── RegisterRequest.java
│   │   │           │   │   │   ├── VerifyEmailRequest.java
│   │   │           │   │   │   ├── SendLoginCodeRequest.java
│   │   │           │   │   │   ├── LoginWithCodeRequest.java
│   │   │           │   │   │   ├── RefreshTokenRequest.java
│   │   │           │   │   │   └── OAuthLoginRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── AuthTokensResponse.java
│   │   │           │   │       └── DeviceSessionResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── User.java
│   │   │           │   │   ├── UserAuthProvider.java
│   │   │           │   │   ├── Session.java
│   │   │           │   │   ├── RefreshToken.java
│   │   │           │   │   ├── DeviceSession.java
│   │   │           │   │   ├── EmailVerification.java
│   │   │           │   │   ├── PasswordResetToken.java
│   │   │           │   │   ├── LoginHistory.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── AuthMethod.java
│   │   │           │   │       ├── OAuthProvider.java
│   │   │           │   │       ├── Platform.java
│   │   │           │   │       ├── LoginStatus.java
│   │   │           │   │       └── VerificationPurpose.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserRepository.java
│   │   │           │   │   ├── UserAuthProviderRepository.java
│   │   │           │   │   ├── SessionRepository.java
│   │   │           │   │   ├── RefreshTokenRepository.java
│   │   │           │   │   ├── DeviceSessionRepository.java
│   │   │           │   │   ├── EmailVerificationRepository.java
│   │   │           │   │   ├── PasswordResetTokenRepository.java
│   │   │           │   │   └── LoginHistoryRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── AuthService.java
│   │   │           │   │   ├── EmailVerificationService.java
│   │   │           │   │   ├── TokenService.java
│   │   │           │   │   ├── DeviceSessionService.java
│   │   │           │   │   ├── SessionService.java
│   │   │           │   │   └── OAuthService.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   ├── JwtTokenProvider.java
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   └── UserPrincipal.java
│   │   │           │   ├── rate_limit/
│   │   │           │   │   ├── RateLimitService.java
│   │   │           │   │   └── RateLimitInterceptor.java
│   │   │           │   └── mapper/
│   │   │           │       └── AuthMapper.java
│   │   │           ├── profile/
│   │   │           │   ├── api/
│   │   │           │   │   └── ProfileController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── UpdateProfileRequest.java
│   │   │           │   │   │   └── UpdatePreferencesRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── ProfileResponse.java
│   │   │           │   │       └── PreferencesResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── UserProfile.java
│   │   │           │   │   └── UserPreferences.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserProfileRepository.java
│   │   │           │   │   └── UserPreferencesRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── UserProfileService.java
│   │   │           │   │   └── UserPreferencesService.java
│   │   │           │   └── mapper/
│   │   │           │       └── ProfileMapper.java
│   │   │           ├── journal/
│   │   │           │   ├── api/
│   │   │           │   │   ├── JournalEntryController.java
│   │   │           │   │   └── TagController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── CreateJournalEntryRequest.java
│   │   │           │   │   │   ├── UpdateJournalEntryRequest.java
│   │   │           │   │   │   ├── CreateTagRequest.java
│   │   │           │   │   │   └── UpdateTagRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── JournalEntryResponse.java
│   │   │           │   │       └── TagResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── JournalEntry.java
│   │   │           │   │   ├── Tag.java
│   │   │           │   │   ├── EntryTag.java
│   │   │           │   │   └── enums/
│   │   │           │   │       └── Mood.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── JournalEntryRepository.java
│   │   │           │   │   ├── TagRepository.java
│   │   │           │   │   └── EntryTagRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── JournalEntryService.java
│   │   │           │   │   ├── TagService.java
│   │   │           │   │   └── EntryTagService.java
│   │   │           │   └── mapper/
│   │   │           │       └── JournalMapper.java
│   │   │           ├── routine/
│   │   │           │   ├── api/
│   │   │           │   │   └── RoutineController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── CreateRoutineRequest.java
│   │   │           │   │   │   ├── UpdateRoutineRequest.java
│   │   │           │   │   │   ├── CreateRoutineEntryRequest.java
│   │   │           │   │   │   └── CreateReminderRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── RoutineResponse.java
│   │   │           │   │       ├── RoutineEntryResponse.java
│   │   │           │   │       └── ReminderResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Routine.java
│   │   │           │   │   ├── RoutineReminder.java
│   │   │           │   │   ├── RoutineEntry.java
│   │   │           │   │   └── enums/
│   │   │           │   │       └── RoutineCategory.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── RoutineRepository.java
│   │   │           │   │   ├── RoutineReminderRepository.java
│   │   │           │   │   └── RoutineEntryRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── RoutineService.java
│   │   │           │   │   ├── RoutineReminderService.java
│   │   │           │   │   └── RoutineEntryService.java
│   │   │           │   └── mapper/
│   │   │           │       └── RoutineMapper.java
│   │   │           ├── calendar/
│   │   │           │   ├── api/
│   │   │           │   │   ├── TimeBlockController.java
│   │   │           │   │   └── CategoryController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── CreateTimeBlockRequest.java
│   │   │           │   │   │   ├── UpdateTimeBlockRequest.java
│   │   │           │   │   │   ├── CreateCategoryRequest.java
│   │   │           │   │   │   └── UpdateCategoryRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── TimeBlockResponse.java
│   │   │           │   │       └── CategoryResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── TimeBlock.java
│   │   │           │   │   ├── Category.java
│   │   │           │   │   └── enums/
│   │   │           │   │       └── Recurrence.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── TimeBlockRepository.java
│   │   │           │   │   └── CategoryRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── TimeBlockService.java
│   │   │           │   │   └── CategoryService.java
│   │   │           │   └── mapper/
│   │   │           │       └── CalendarMapper.java
│   │   │           ├── media/
│   │   │           │   ├── api/
│   │   │           │   │   └── MediaController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── CreateUploadUrlRequest.java
│   │   │           │   │   │   ├── CompleteUploadRequest.java
│   │   │           │   │   │   └── AttachMediaRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── UploadUrlResponse.java
│   │   │           │   │       └── MediaFileResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── MediaFile.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── MediaType.java
│   │   │           │   │       └── ProcessingStatus.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── MediaFileRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── S3Service.java
│   │   │           │   │   └── MediaService.java
│   │   │           │   └── mapper/
│   │   │           │       └── MediaMapper.java
│   │   │           ├── gamification/
│   │   │           │   ├── api/
│   │   │           │   │   ├── UserStatsController.java
│   │   │           │   │   ├── AchievementController.java
│   │   │           │   │   ├── StreakController.java
│   │   │           │   │   └── StoreController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   └── PurchaseItemRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── UserStatsResponse.java
│   │   │           │   │       ├── AchievementResponse.java
│   │   │           │   │       ├── UserAchievementResponse.java
│   │   │           │   │       ├── StreakResponse.java
│   │   │           │   │       ├── StreakHistoryResponse.java
│   │   │           │   │       ├── StoreItemResponse.java
│   │   │           │   │       └── CoinTransactionResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── UserStats.java
│   │   │           │   │   ├── Streak.java
│   │   │           │   │   ├── StreakHistory.java
│   │   │           │   │   ├── Achievement.java
│   │   │           │   │   ├── UserAchievement.java
│   │   │           │   │   ├── StoreItem.java
│   │   │           │   │   ├── UserPurchase.java
│   │   │           │   │   ├── CoinTransaction.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── StreakType.java
│   │   │           │   │       ├── StreakEventType.java
│   │   │           │   │       ├── AchievementType.java
│   │   │           │   │       ├── RequirementType.java
│   │   │           │   │       ├── Rarity.java
│   │   │           │   │       ├── ItemType.java
│   │   │           │   │       └── CoinTransactionType.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserStatsRepository.java
│   │   │           │   │   ├── StreakRepository.java
│   │   │           │   │   ├── StreakHistoryRepository.java
│   │   │           │   │   ├── AchievementRepository.java
│   │   │           │   │   ├── UserAchievementRepository.java
│   │   │           │   │   ├── StoreItemRepository.java
│   │   │           │   │   ├── UserPurchaseRepository.java
│   │   │           │   │   └── CoinTransactionRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── UserStatsService.java
│   │   │           │   │   ├── StreakService.java
│   │   │           │   │   ├── AchievementService.java
│   │   │           │   │   ├── StoreService.java
│   │   │           │   │   ├── CoinService.java
│   │   │           │   │   └── LevelService.java
│   │   │           │   └── mapper/
│   │   │           │       └── GamificationMapper.java
│   │   │           ├── subscription/
│   │   │           │   ├── api/
│   │   │           │   │   ├── SubscriptionController.java
│   │   │           │   │   └── PurchaseController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   └── VerifyReceiptRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── SubscriptionPlanResponse.java
│   │   │           │   │       ├── SubscriptionResponse.java
│   │   │           │   │       └── ReceiptResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── SubscriptionPlan.java
│   │   │           │   │   ├── UserSubscription.java
│   │   │           │   │   ├── Receipt.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── SubscriptionStatus.java
│   │   │           │   │       ├── BillingPeriod.java
│   │   │           │   │       ├── StorePlatform.java
│   │   │           │   │       └── ValidationStatus.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── SubscriptionPlanRepository.java
│   │   │           │   │   ├── UserSubscriptionRepository.java
│   │   │           │   │   └── ReceiptRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── SubscriptionService.java
│   │   │           │   │   ├── ReceiptService.java
│   │   │           │   │   └── EntitlementService.java
│   │   │           │   ├── idempotency/
│   │   │           │   │   ├── IdempotencyService.java
│   │   │           │   │   └── IdempotencyInterceptor.java
│   │   │           │   └── mapper/
│   │   │           │       └── SubscriptionMapper.java
│   │   │           ├── notification/
│   │   │           │   ├── api/
│   │   │           │   │   ├── DeviceController.java
│   │   │           │   │   └── NotificationController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   └── RegisterDeviceRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── DeviceResponse.java
│   │   │           │   │       └── NotificationLogResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── UserDevice.java
│   │   │           │   │   ├── NotificationLog.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── NotificationType.java
│   │   │           │   │       └── DeliveryStatus.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserDeviceRepository.java
│   │   │           │   │   └── NotificationLogRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── DeviceService.java
│   │   │           │   │   ├── FcmService.java
│   │   │           │   │   └── NotificationDispatcher.java
│   │   │           │   └── mapper/
│   │   │           │       └── NotificationMapper.java
│   │   │           ├── sync/
│   │   │           │   ├── api/
│   │   │           │   │   └── SyncController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── SyncPushRequest.java
│   │   │           │   │   │   └── SyncPullRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── SyncPushResponse.java
│   │   │           │   │       └── SyncPullResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── SyncQueue.java
│   │   │           │   │   ├── SyncMetadata.java
│   │   │           │   │   └── enums/
│   │   │           │   │       ├── SyncEntityType.java
│   │   │           │   │       ├── SyncAction.java
│   │   │           │   │       └── SyncStatus.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── SyncQueueRepository.java
│   │   │           │   │   └── SyncMetadataRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── SyncService.java
│   │   │           │   │   └── ConflictResolver.java
│   │   │           │   └── mapper/
│   │   │           │       └── SyncMapper.java
│   │   │           ├── analytics/
│   │   │           │   ├── api/
│   │   │           │   │   └── AnalyticsController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── request/
│   │   │           │   │   │   ├── CreateWeeklyReviewRequest.java
│   │   │           │   │   │   └── UpdateWeeklyReviewRequest.java
│   │   │           │   │   └── response/
│   │   │           │   │       ├── WeeklyReviewResponse.java
│   │   │           │   │       ├── DailyUsageResponse.java
│   │   │           │   │       └── AnalyticsSummaryResponse.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── WeeklyReview.java
│   │   │           │   │   └── DailyUsage.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── WeeklyReviewRepository.java
│   │   │           │   │   └── DailyUsageRepository.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── WeeklyReviewService.java
│   │   │           │   │   ├── DailyUsageService.java
│   │   │           │   │   └── AnalyticsService.java
│   │   │           │   └── mapper/
│   │   │           │       └── AnalyticsMapper.java
│   │   │           ├── scheduler/
│   │   │           │   ├── config/
│   │   │           │   │   └── SchedulerConfig.java
│   │   │           │   └── jobs/
│   │   │           │       ├── OrphanMediaCleanupJob.java
│   │   │           │       ├── ReminderDispatchJob.java
│   │   │           │       ├── SubscriptionExpiryJob.java
│   │   │           │       ├── StreakBreakCheckJob.java
│   │   │           │       ├── DailyUsageResetJob.java
│   │   │           │       └── ExpiredSessionCleanupJob.java
│   │   │           └── audit/
│   │   │               ├── domain/
│   │   │               │   ├── AuditLog.java
│   │   │               │   └── enums/
│   │   │               │       ├── AuditAction.java
│   │   │               │       └── AuditStatus.java
│   │   │               ├── repository/
│   │   │               │   └── AuditLogRepository.java
│   │   │               └── service/
│   │   │                   └── AuditLogService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_schemas.sql
│   │               ├── V2__auth_tables.sql
│   │               ├── V3__profile_tables.sql
│   │               ├── V4__subscription_tables.sql
│   │               ├── V5__journal_tables.sql
│   │               ├── V6__routine_tables.sql
│   │               ├── V7__calendar_tables.sql
│   │               ├── V8__gamification_tables.sql
│   │               ├── V9__notification_tables.sql
│   │               ├── V10__sync_tables.sql
│   │               ├── V11__analytics_tables.sql
│   │               ├── V12__indexes.sql
│   │               └── R__seed_data.sql
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── thatmoment/
│       │           ├── ThatMomentApplicationTests.java
│       │           ├── common/
│       │           │   ├── TestcontainersConfig.java
│       │           │   ├── BaseIntegrationTest.java
│       │           │   └── TestFixtures.java
│       │           ├── auth/
│       │           │   ├── AuthControllerTest.java
│       │           │   ├── AuthServiceTest.java
│       │           │   └── JwtTokenProviderTest.java
│       │           ├── profile/
│       │           │   ├── ProfileControllerTest.java
│       │           │   └── ProfileServiceTest.java
│       │           ├── journal/
│       │           │   ├── JournalEntryControllerTest.java
│       │           │   ├── JournalEntryServiceTest.java
│       │           │   └── TagServiceTest.java
│       │           ├── routine/
│       │           │   ├── RoutineControllerTest.java
│       │           │   └── RoutineServiceTest.java
│       │           ├── calendar/
│       │           │   ├── TimeBlockControllerTest.java
│       │           │   └── TimeBlockServiceTest.java
│       │           ├── media/
│       │           │   ├── MediaControllerTest.java
│       │           │   └── S3ServiceTest.java
│       │           ├── gamification/
│       │           │   ├── StreakServiceTest.java
│       │           │   ├── CoinServiceTest.java
│       │           │   └── AchievementServiceTest.java
│       │           ├── subscription/
│       │           │   ├── SubscriptionControllerTest.java
│       │           │   └── EntitlementServiceTest.java
│       │           ├── notification/
│       │           │   └── FcmServiceTest.java
│       │           ├── sync/
│       │           │   ├── SyncControllerTest.java
│       │           │   └── ConflictResolverTest.java
│       │           └── analytics/
│       │               └── AnalyticsServiceTest.java
│       └── resources/
│           ├── application-test.yml
│           └── fixtures/
│               ├── users.json
│               ├── journal_entries.json
│               └── routines.json
```