package com.bigong.oguri.data.model

enum class UserRoleType {
    OFFICE_WORKER,
    STUDENT,
}

enum class WorkScheduleType {
    FIVE_DAYS,
    SIX_DAYS,
}

enum class LoginProviderType {
    NONE,
    KAKAO,
    APPLE,
    GUEST,
}

enum class SupportInquiryType {
    FEATURE_SUGGESTION,
    BUG_REPORT,
    OTHER_INQUIRY,
}

data class UserMvpState(
    val userRoleType: UserRoleType = UserRoleType.OFFICE_WORKER,
    val remainingAnnualLeaveDays: Int = 15,
    val workScheduleType: WorkScheduleType = WorkScheduleType.FIVE_DAYS,
    val loginProviderType: LoginProviderType = LoginProviderType.NONE,
    val isOnboardingCompleted: Boolean = false,
    val isProSubscribed: Boolean = false,
    val savedStrategyIdentifierList: List<String> = listOf("2026-10-best", "2026-09-long-week", "2026-05-family-trip"),
)

data class SupportEmailDraft(
    val subjectText: String,
    val bodyText: String,
)
