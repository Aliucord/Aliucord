package com.aliucord.wrappers.users

import com.aliucord.utils.accessField
import com.discord.api.user.AvatarDecoration
import com.discord.api.user.Collectibles
import com.discord.models.member.GuildMember
import com.discord.api.guildmember.GuildMember as ApiGuildMember

var ApiGuildMember.avatarDecorationData by accessField<AvatarDecoration?>()
var GuildMember.avatarDecorationData by accessField<AvatarDecoration?>()

var ApiGuildMember.collectibles by accessField<Collectibles?>()
var GuildMember.collectibles by accessField<Collectibles?>()
