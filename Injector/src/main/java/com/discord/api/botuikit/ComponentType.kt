package com.discord.api.botuikit

// This entirely replaces Discord's stock com.discord.api.botuikit to add more Component types
enum class ComponentType(val type: Int, val clazz: Class<*>) {
    UNKNOWN(0, UnknownComponent::class.java),
    ACTION_ROW(1, ActionRowComponent::class.java),
    BUTTON(2, ButtonComponent::class.java),
    SELECT(3, SelectComponent::class.java),
    TEXT(4, TextComponent::class.java),
    USER_SELECT(5, UserSelectComponent::class.java),
    ROLE_SELECT(6, RoleSelectComponent::class.java),
    MENTIONABLE_SELECT(7, MentionableSelectComponent::class.java),
    CHANNEL_SELECT(8, ChannelSelectComponent::class.java),
    SECTION(9, SectionComponent::class.java),
    TEXT_DISPLAY(10, TextDisplayComponent::class.java),
    THUMBNAIL(11, ThumbnailComponent::class.java),
    MEDIA_GALLERY(12, MediaGalleryComponent::class.java),
    FILE(13, FileComponent::class.java),
    SEPARATOR(14, SeparatorComponent::class.java),
    CONTAINER(17, ContainerComponent::class.java),
    LABEL(18, LabelComponent::class.java),
    FILE_UPLOAD(19, FileUploadComponent::class.java),
}
