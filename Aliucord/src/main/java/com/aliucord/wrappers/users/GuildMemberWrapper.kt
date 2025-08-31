package com.aliucord.wrappers.users

import com.aliucord.utils.accessField
import com.discord.api.user.AvatarDecoration
import com.discord.api.user.Collectibles
import com.discord.models.member.GuildMember
import com.discord.api.guildmember.GuildMember as ApiGuildMember

var ApiGuildMember.avatarDecorationData by accessField<ApiGuildMember, AvatarDecoration?>()
var GuildMember.avatarDecorationData by accessField<GuildMember, AvatarDecoration?>()

var ApiGuildMember.collectibles by accessField<ApiGuildMember, Collectibles?>()
var GuildMember.collectibles by accessField<GuildMember, Collectibles?>()
