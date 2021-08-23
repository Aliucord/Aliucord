package com.aliucord.api.kotlin.data

import com.discord.api.commands.ApplicationCommandType
import com.discord.api.commands.CommandChoice
import com.discord.models.commands.ApplicationCommandOption

fun applicationCommandOption(
    type: ApplicationCommandType,
    name: String,
    description: String,
    descriptionRes: Int? = null,
    required: Boolean = false,
    def: Boolean = false,
    choices: List<CommandChoice>? = null,
    options: List<ApplicationCommandOption>?  = null
) = ApplicationCommandOption(
    type,
    name,
    description,
    descriptionRes,
    required,
    def,
    choices,
    options
)