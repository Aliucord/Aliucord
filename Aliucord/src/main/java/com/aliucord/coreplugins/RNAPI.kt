/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.utils.accessField
import com.aliucord.wrappers.embeds.MessageEmbedWrapper.Companion.rawVideo
import com.aliucord.wrappers.users.globalName
import com.discord.api.channel.Channel
import com.discord.api.channel.`ChannelUtils$getDisplayName$1`
import com.discord.api.message.embed.EmbedType
import com.discord.api.message.embed.MessageEmbed
import com.discord.api.role.GuildRoleColors
import com.discord.api.sticker.*
import com.discord.api.user.UserProfile
import com.discord.databinding.*
import com.discord.models.domain.Model
import com.discord.models.member.GuildMember
import com.discord.models.presence.Presence
import com.discord.models.user.CoreUser
import com.discord.models.user.MeUser
import com.discord.utilities.auth.`AuthUtils$createDiscriminatorInputValidator$1`
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.lazy.memberlist.MemberListRow
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload
import com.discord.utilities.mg_recycler.SingleTypePayload
import com.discord.utilities.search.suggestion.entries.UserSuggestion
import com.discord.utilities.user.UserUtils
import com.discord.views.user.SettingsMemberView
import com.discord.widgets.channels.memberlist.ThreadMemberListItemGeneratorKt
import com.discord.widgets.chat.input.autocomplete.UserAutocompletable
import com.discord.widgets.chat.input.autocomplete.adapter.AutocompleteItemViewHolder
import com.discord.widgets.friends.FriendsListViewModel
import com.discord.widgets.friends.WidgetFriendsListAdapter
import com.discord.widgets.search.suggestions.WidgetSearchSuggestionsAdapter
import com.discord.widgets.settings.account.WidgetSettingsAccountUsernameEdit
import com.discord.widgets.user.*
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import rx.Observable
import java.lang.reflect.Type
import java.text.Collator
import java.util.*
import com.discord.api.user.User as APIUser
import com.discord.models.user.User as ModelUser

@Suppress("UNCHECKED_CAST")
internal class RNAPI : CorePlugin(Manifest("RNAPI")) {
    override val isHidden = true
    override val isRequired = true

    var UserProfileHeaderViewModel.ViewState.Loaded.showAkasState by accessField<Boolean>("showAkas")
    var AutocompleteItemViewHolder.binding by accessField<WidgetChatInputAutocompleteItemBinding>()
    val WidgetFriendsListAdapter.ItemUser.binding by accessField<WidgetFriendsListAdapterItemFriendBinding>()
    val WidgetFriendsListAdapter.ItemPendingUser.binding by accessField<WidgetFriendsListAdapterItemPendingBinding>()
    val WidgetUserMutualFriends.MutualFriendsAdapter.ViewHolder.binding by accessField<WidgetUserProfileAdapterItemFriendBinding>()
    val WidgetSearchSuggestionsAdapter.UserViewHolder.binding by accessField<WidgetSearchSuggestionsItemUserBinding>()
    var MessageEmbed.type by accessField<EmbedType>()
    var UserAutocompletable.matchers by accessField<List<String>>("textMatchers")
    val TreeSet<*>.map by accessField<TreeMap<*, *>>("m")
    var TreeMap<*, *>.comparator by accessField<Comparator<*>>()

    override fun load(context: Context) {
        XposedBridge.makeClassInheritable(UserProfile::class.java)
    }

    override fun start(context: Context) {
        if (ManagerBuild.hasPatches("1.1.1")) patchGlobalName()
        else logger.warn("Base app outdated, cannot patch display names")

        patchNextCallAdapter()
        patchUserProfile()
        patchDefaultAvatars()
        patchDiscriminator()
        patchStickers()
        patchMessageEmbeds()
        patchSorting()

        if (ManagerBuild.hasInjector("2.1.2")) patchAuditLog()
        else logger.warn("Base app outdated, cannot patch audit log")
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    fun patchNextCallAdapter() {
        val oldUserProfile = TypeToken.getParameterized(Observable::class.java, UserProfile::class.java).type
        val newUserProfile = TypeToken.getParameterized(Observable::class.java, RNUserProfile::class.java).type

        // nextCallAdapter https://github.com/square/retrofit/blob/c0fd64b5d3ddcc6665a16a4814c5b1596762305d/retrofit/src/main/java/retrofit2/Retrofit.java#L252
        patcher.before<i0.y>("a",
            Type::class.java,
            Array<Annotation>::class.java)
        { (param, userType: Type) ->
            if (oldUserProfile == userType) param.args[0] = newUserProfile
        }
    }

    fun patchGlobalName() {
        patcher.after<CoreUser>(APIUser::class.java) { (_, user: APIUser) ->
            this.globalName = user.globalName
        }
        patcher.after<CoreUser.Companion>("merge",
            CoreUser::class.java,
            APIUser::class.java
        ) { (param, coreUser: CoreUser, apiUser: APIUser) ->
            val resultUser = param.result as? CoreUser ?: return@after
            resultUser.globalName = apiUser.globalName ?: coreUser.globalName ?: return@after
        }
        patcher.after<MeUser>(
            APIUser::class.java
        ) { (_, user: APIUser) ->
            this.globalName = user.globalName
        }
        patcher.after<MeUser.Companion>("merge",
            MeUser::class.java,
            APIUser::class.java
        ) { (param, meUser: MeUser, apiUser: APIUser) ->
            val resultUser = param.result as? MeUser ?: return@after
            resultUser.globalName = apiUser.globalName ?: meUser.globalName ?: return@after
        }
        patcher.before<`ChannelUtils$getDisplayName$1`>("invoke",
            Any::class.java
        ) { (param, apiUser: APIUser) ->
            param.result = apiUser.globalName ?: return@before
        }
        patcher.after<UserAutocompletable>(
            ModelUser::class.java,
            GuildMember::class.java,
            String::class.javaObjectType,
            Presence::class.java,
            Boolean::class.java
        ) { (_, modelUser: ModelUser, _: GuildMember) ->
            modelUser.globalName?.let { globalName ->
                this.matchers = listOf(
                    leadingIdentifier().identifier + modelUser.username,
                    leadingIdentifier().identifier + globalName,
                )
            }
        }
        patcher.before<UserUtils>("padDiscriminator",
            Int::class.java
        ) { (param, discrim: Int) ->
            if (discrim == 0) param.result = ""
        }
        patcher.after<GuildMember.Companion>("getNickOrUsername",
            ModelUser::class.java,
            GuildMember::class.java,
            Channel::class.java,
            List::class.java
        ) { (param, modelUser: ModelUser) ->
            if (param.result == modelUser.username) param.result = modelUser.globalName ?: return@after
        }
        patcher.before<UserNameFormatterKt?>("getSpannableForUserNameWithDiscrim",
            ModelUser::class.java,
            String::class.javaObjectType,
            Context::class.java,
            Int::class.java,
            Int::class.java,
            Int::class.java,
            Int::class.java,
            Int::class.java,
            Int::class.java
        ) { (param, modelUser: ModelUser, nickname: String?) ->
            if (nickname == null) param.args[1] = modelUser.globalName ?: return@before
        }

        patcher.before<UserProfileHeaderView>("getSecondaryNameTextForUser",
            ModelUser::class.java,
            GuildMember::class.java
        ) { (param, modelUser: ModelUser) ->
            if (modelUser.globalName == null) return@before
            param.result =
                if (modelUser.discriminator == 0) modelUser.username
                else modelUser.username + UserUtils.INSTANCE.getDiscriminatorWithPadding(modelUser)
        }

        var showAkas = false
        patcher.before<UserProfileHeaderView>("configureSecondaryName",
            UserProfileHeaderViewModel.ViewState.Loaded::class.java
        ) { (_, viewState: UserProfileHeaderViewModel.ViewState.Loaded) ->
            showAkas = viewState.showAkasState
            viewState.showAkasState = false
        }
        patcher.after<UserProfileHeaderView>("configureSecondaryName",
            UserProfileHeaderViewModel.ViewState.Loaded::class.java
        ) { (_, viewState: UserProfileHeaderViewModel.ViewState.Loaded) ->
            viewState.showAkasState = showAkas
        }
        patcher.after<WidgetFriendsListAdapter.ItemUser>("onConfigure",
            Int::class.java,
            FriendsListViewModel.Item::class.java
        ) { (_, _: Int, item: FriendsListViewModel.Item.Friend) ->
            binding.f.text = item.user.globalName ?: return@after
        }
        patcher.after<WidgetFriendsListAdapter.ItemPendingUser>("onConfigure",
            Int::class.java,
            FriendsListViewModel.Item::class.java
        ) { (_, _: Int, item: FriendsListViewModel.Item.PendingFriendRequest) ->
            binding.f.text = item.user.globalName ?: return@after
        }
        patcher.after<WidgetUserMutualFriends.MutualFriendsAdapter.ViewHolder>("onConfigure",
            Int::class.java,
            WidgetUserMutualFriends.Model.Item::class.java
        ) { (_, _: Int, item: WidgetUserMutualFriends.Model.Item.MutualFriend) ->
            binding.i.text = item.user.globalName ?: return@after
        }
        patcher.after<WidgetSearchSuggestionsAdapter.UserViewHolder>("onConfigure",
            Int::class.java,
            MGRecyclerDataPayload::class.java
        ) { (_, _: Int, payload: SingleTypePayload<UserSuggestion>) ->
            val name = payload.data.user.globalName ?: return@after
            if (payload.data.nickname == null) binding.b.k.apply {
                d.text = c.text
                c.text = name
            }
        }
        patcher.after<SettingsMemberView>("a",
            ModelUser::class.java,
            GuildMember::class.java
        ) { (_, modelUser: ModelUser, guildMember: GuildMember?) ->
            if (guildMember?.nick != null) return@after
            val name = modelUser.globalName ?: return@after
            this.j.d.j.c.text = name
            this.j.c.visibility = View.VISIBLE
        }
        patcher.after<AutocompleteItemViewHolder>("bindUser",
            UserAutocompletable::class.java
        ) { (_, autocomplete: UserAutocompletable) ->
            if (autocomplete.nickname == null) binding.e.text = autocomplete.user.globalName ?: return@after
        }
    }

    fun patchDefaultAvatars() {
        patcher.instead<IconUtils?>("getForUser",
            Long::class.javaObjectType,
            String::class.javaObjectType,
            Int::class.javaObjectType,
            Boolean::class.java,
            Int::class.javaObjectType
        ) { (_, id: Long?, avatar: String?, discrim: Int?, animated: Boolean, size: Int?) ->
            if (avatar != null && id != null) {
                // webp fix takes care of animated query
                val ext = IconUtils.INSTANCE.getImageExtension(avatar, animated)
                "https://cdn.discordapp.com/avatars/$id/$avatar.$ext" +
                    (size?.let { "?size=${IconUtils.getMediaProxySize(size)}" } ?: "")
            } else {
                "asset://asset/images/default_avatar_" +
                    (discrim?.takeUnless { it == 0 }?.mod(5) ?: ((id ?: 0) shr 22).mod(6)) +
                    ".png"
            }
        }
    }

    fun patchDiscriminator() {
        patcher.instead<`AuthUtils$createDiscriminatorInputValidator$1`>("getErrorMessage",
            TextInputLayout::class.java
        ) {}
        patcher.after<WidgetSettingsAccountUsernameEdit>("configureUI",
            MeUser::class.java
        ) {
            val binding = WidgetSettingsAccountUsernameEdit.`access$getBinding$p`(this)
            (binding.b.parent as View).visibility = View.GONE
        }
        patcher.before<WidgetUserPasswordVerify>("updateAccountInfo",
            String::class.javaObjectType
        ) { this.mostRecentIntent.removeExtra("INTENT_EXTRA_DISCRIMINATOR") }
    }

    fun patchUserProfile() {
        /** discord doesn't check in [com.discord.widgets.user.WidgetUserMutualGuilds.Model] if mutualGuilds list is null */
        patcher.after<UserProfile>("d") { param ->
            if (param.result == null) param.result = Collections.EMPTY_LIST
        }
    }

    fun patchStickers() {
        val pngHook: Any.(XC_MethodHook.MethodHookParam) -> Unit = {
            if (it.result == StickerFormatType.UNKNOWN) it.result = StickerFormatType.PNG
        }
        patcher.after<Sticker>("a", callback = pngHook)
        patcher.after<StickerPartial>("a", callback = pngHook)

        val gifHook: Any.(XC_MethodHook.MethodHookParam) -> Unit = {
            if (it.result == "") it.result = ".gif"
        }
        patcher.after<Sticker>("b", callback = gifHook)
        patcher.after<StickerPartial>("b", callback = gifHook)
    }

    fun patchMessageEmbeds() {
        patcher.after<MessageEmbed>("k") { param ->
            if (param.result == EmbedType.RICH && this.rawVideo != null) {
                param.result = EmbedType.VIDEO
                this.type = EmbedType.VIDEO
            }
        }
    }

    // TODO: display gradient changes for role colors
    fun patchAuditLog() {
        patcher.before<Model.JsonReader>("parseUnknown",
            Model.JsonReader.ItemFactory::class.java
        ) { param ->
            if (this.peek() == JsonToken.l) {
                val colors = GuildRoleColors()
                this.nextObject { field ->
                    when (field) {
                        "primary_color" -> colors.primaryColor = this.nextInt(0)
                        "secondary_color" -> colors.secondaryColor = this.nextIntOrNull()
                        "tertiary_color" -> colors.tertiaryColor = this.nextIntOrNull()
                        else -> this.skipValue()
                    }
                }
                param.result = colors
            }
        }
    }

    fun patchSorting() {
        // Fix sorting for thread member list
        patcher.after<ThreadMemberListItemGeneratorKt?>("initializeOrderedMap",
            Map::class.java
        ) { param ->
            val result = param.result as Map<*, TreeSet<MemberListRow.Member>>
            result.keys.forEach {
                val treeSet = result[it] ?: return@forEach
                treeSet.map.comparator = THREAD_MEMBER_COMPARATOR
            }
        }

        // Fix sorting for friends list
        patcher.after<FriendsListViewModel>("getItems",
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Boolean::class.java,
            Map::class.java
        ) { param ->
            val sections = param.result as FriendsListViewModel.ListSections
            val friendsWithHeaders = sections.friendsItemsWithHeaders
            if (friendsWithHeaders.isNullOrEmpty()) return@after

            // collect headers with indexes to split main list
            val headersAndSize = friendsWithHeaders
                .withIndex()
                .filter { (_, item) -> item is FriendsListViewModel.Item.Header }
                .associate { (index, item) -> index to item as FriendsListViewModel.Item.Header }
            headersAndSize.forEach { (headerIndex, header) ->
                val friendChunk =
                    friendsWithHeaders.subList(headerIndex + 1, headerIndex + header.count + 1) as MutableList<FriendsListViewModel.Item.Friend>
                when (header.titleStringResId) {
                    R.h.friends_online_header -> {
                        friendChunk.sortWith(FRIENDS_COMPARATOR)
                    }

                    R.h.friends_offline_header -> {
                        friendChunk.sortWith(FRIENDS_COMPARATOR)
                    }
                }
            }
            val pendingFriends = sections.pendingItems as? MutableList<FriendsListViewModel.Item.PendingFriendRequest>
            if (pendingFriends.isNullOrEmpty()) return@after
            pendingFriends.sortWith(PENDING_FRIENDS_COMPARATOR)
        }

        // Add sorting to mutual friends list
        patcher.before<WidgetUserMutualFriends>("configureUI",
            WidgetUserMutualFriends.Model::class.java
        ) { (_, model: WidgetUserMutualFriends.Model) ->
            val items = model.items as? MutableList<WidgetUserMutualFriends.Model.Item.MutualFriend>
            if (items.isNullOrEmpty()) return@before
            items.sortWith(MUTUAL_FRIEND_COMPARATOR)
        }
    }

    companion object {
        const val PENDING_REQUEST_INCOMING = 3
        const val PENDING_REQUEST_OUTGOING = 4

        private val COLLATOR = Collator.getInstance().apply { strength = Collator.PRIMARY }
        private val THREAD_MEMBER_COMPARATOR = Comparator<MemberListRow.Member> { a, b ->
            COLLATOR.compare(a?.name, b?.name)
        }
        private val FRIENDS_COMPARATOR = Comparator<FriendsListViewModel.Item.Friend> { a, b ->
            COLLATOR.compare(
                a?.user?.globalName ?: a?.user?.username,
                b?.user?.globalName ?: b?.user?.username
            )
        }
        private val PENDING_FRIENDS_COMPARATOR = Comparator<FriendsListViewModel.Item.PendingFriendRequest> { a, b ->
            // prioritize incoming request just like original impl - Canny
            if (a.relationshipType == PENDING_REQUEST_INCOMING && b.relationshipType == PENDING_REQUEST_OUTGOING) return@Comparator -1
            if (a.relationshipType == PENDING_REQUEST_OUTGOING && b.relationshipType == PENDING_REQUEST_INCOMING) return@Comparator 1
            COLLATOR.compare(
                a?.user?.globalName ?: a?.user?.username,
                b?.user?.globalName ?: b?.user?.username
            )
        }
        private val MUTUAL_FRIEND_COMPARATOR = Comparator<WidgetUserMutualFriends.Model.Item.MutualFriend> { a, b ->
            COLLATOR.compare(
                a?.user?.globalName ?: a?.user?.username,
                b?.user?.globalName ?: b?.user?.username
            )
        }
    }
}
