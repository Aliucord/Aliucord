package com.aliucord.coreplugins.decorations.guildtags

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentManager
import com.aliucord.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.views.Button
import com.aliucord.widgets.BottomSheet
import com.aliucord.wrappers.users.primaryGuild
import com.discord.api.commands.Application
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.drawable.DrawableCompat
import com.discord.utilities.images.MGImages
import com.discord.utilities.resources.StringResourceUtilsKt
import com.discord.utilities.string.StringUtilsKt
import com.discord.utilities.textprocessing.node.EmojiNode
import com.discord.utilities.view.extensions.ViewExtensions
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.lytefast.flexinput.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import b.a.k.b as FormatUtils

private val logger = Logger("Decorations/GuildTag")

internal class GuildProfileSheet : BottomSheet() {
    companion object {
        const val BUNDLE_KEY = "com.aliucord.GuildProfileSheet.guildID"
        fun show(fragmentManager: FragmentManager, guildId: Long) {
            val sheet = GuildProfileSheet()
            val bundle = Bundle()
            bundle.putLong(BUNDLE_KEY, guildId)
            sheet.arguments = bundle
            sheet.show(fragmentManager, GuildProfileSheet::class.java.name)
        }
    }

    private lateinit var loadingIndicator: FrameLayout
    private lateinit var container: ConstraintLayout
    private lateinit var banner: SimpleDraweeView
    private lateinit var icon: SimpleDraweeView
    private lateinit var iconContainer: MaterialCardView
    private lateinit var name: TextView
    private lateinit var countsContainer: LinearLayout
    private lateinit var onlineCount: TextView
    private lateinit var memberCount: TextView
    private lateinit var established: TextView
    private lateinit var description: TextView
    private lateinit var games: LinearLayout
    private lateinit var traits: ChipGroup
    private lateinit var actionButton: Button

    private val bannerId = View.generateViewId()
    private val iconContainerId = View.generateViewId()
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val ctx = view.context
        val sizeF = resources.getDimension(R.d.avatar_wrap_size_xxlarge)
        val size = sizeF.toInt()
        loadingIndicator = FrameLayout(ctx).addTo(linearLayout) {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            ProgressBar(ctx).addTo(this) {
                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
                setPadding(0, 16.dp, 0, 16.dp)
            }
        }
        container = ConstraintLayout(ctx).addTo(linearLayout) {
            setPadding(0, 0, 0, 16.dp)
            visibility = View.GONE
            banner = SimpleDraweeView(ctx).addTo(this) {
                id = bannerId
                layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    topToTop = PARENT_ID
                }
                aspectRatio = 5f
            }
            val color = ColorCompat.getThemedColor(this, R.b.colorSurface)
            val st = 4.dp
            iconContainer = MaterialCardView(ctx).addTo(this) {
                id = iconContainerId
                strokeColor = color
                strokeWidth = st
                layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                    startToStart = bannerId
                    topToBottom = bannerId
                    bottomToBottom = bannerId
                    marginStart = 16.dp
                }
                elevation = 0f
                setCardBackgroundColor(color)
                radius = sizeF / 3
                icon = SimpleDraweeView(ctx).addTo(this) {
                    layoutParams = FrameLayout.LayoutParams(size - (st * 2), WRAP_CONTENT).apply {
                        gravity = Gravity.CENTER
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    aspectRatio = 1f
                    MGImages.setRoundingParams(this, (sizeF - (st * 2)) / 3, false, null, null, null)
                }
            }
            LinearLayout(ctx).addTo(this) {
                orientation = LinearLayout.VERTICAL
                layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    marginStart = 16.dp
                    marginEnd = 16.dp
                    topMargin = 8.dp
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    topToBottom = iconContainerId
                }
                name = TextView(ctx, null, 0, R.i.UiKit_TextView_H1_Bold).addTo(this) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_xxlarge))
                }
                countsContainer = LinearLayout(ctx).addTo(this) {
                    setPadding(0.dp, 8.dp, 0, 0)
                    visibility = View.GONE
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    CardView(ctx).addTo(this) {
                        layoutParams = LinearLayout.LayoutParams(8.dp, 8.dp)
                        radius = 4.dp.toFloat()
                        elevation = 0f
                        setCardBackgroundColor(ColorCompat.getColor(this, R.c.status_green_600))
                    }
                    onlineCount = TextView(ctx, null, 0, R.i.GuildProfileSheet_DiscoverableGuild_Text).addTo(this) {
                        setPadding(6.dp, 0, 0, 0)
                    }
                    CardView(ctx).addTo(this) {
                        layoutParams = LinearLayout.LayoutParams(8.dp, 8.dp).apply {
                            marginStart = 16.dp
                        }
                        radius = 4.dp.toFloat()
                        elevation = 0f
                        setCardBackgroundColor(ColorCompat.getColor(this, R.c.status_grey_500))
                    }
                    memberCount = TextView(ctx, null, 0, R.i.GuildProfileSheet_DiscoverableGuild_Text).addTo(this) {
                        setPadding(6.dp, 0, 0, 0)
                    }
                }
                established = TextView(ctx, null, 0, R.i.GuildProfileSheet_DiscoverableGuild_Text).addTo(this) {
                    setPadding(0.dp, 4.dp, 0, 0)
                    visibility = View.GONE
                }
                description = TextView(ctx, null, 0, R.i.UiKit_TextView).addTo(this) {
                    setPadding(0.dp, 12.dp, 0, 0)
                    visibility = View.GONE
                }
                games = LinearLayout(ctx).addTo(this) {
                    setPadding(0.dp, 14.dp, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    visibility = View.GONE
                    gravity = Gravity.CENTER_VERTICAL
                }
                traits = ChipGroup(ctx).addTo(this) {
                    setPadding(0.dp, 12.dp, 0, 0)
                    visibility = View.GONE
                    chipSpacingVertical = 8.dp
                    chipSpacingHorizontal = 8.dp
                }
                actionButton = Button(ctx).addTo(this) {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                        topMargin = 12.dp
                    }
                    visibility = View.GONE
                }
            }
        }

        val guildId = arguments?.getLong(BUNDLE_KEY) ?: return
        GuildProfileStore.get(guildId) {
            when (it) {
                is GuildProfileStore.ProfileResult.Private -> configureUnavailable()
                is GuildProfileStore.ProfileResult.Failed -> onError(it.id, it.exception)
                is GuildProfileStore.ProfileResult.Available -> configure(it.profile, it.applications)
            }

            loadingIndicator.visibility = View.GONE
            container.visibility = View.VISIBLE
        }
    }

    private fun onError(id: Long, exception: Http.HttpException) {
        logger.error("Error while fetching profile for guild $id", exception)
        Utils.showToast("An error occurred while fetching server profile")
        dismiss()
    }

    private fun configureUnavailable() {
        name.text = "Private Server"
        ViewExtensions.setTextAndVisibilityBy(description, "The server has limited who can see this profile.")
        val drawable = DrawableCompat.getThemedDrawableRes(icon, R.b.img_poop)
        icon.setImageResource(drawable)
    }

    private val estDateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private fun configure(profile: GuildProfile, applications: List<Application>) {
        icon.setImageURI("https://cdn.discordapp.com/icons/${profile.id}/${profile.iconHash}.png?size=256")
        name.text = profile.name
        val year = estDateFormat.format(Date(SnowflakeUtils.toTimestamp(profile.id)))
        ViewExtensions.setTextAndVisibilityBy(established, "Est. $year")
        ViewExtensions.setTextAndVisibilityBy(description, profile.description)

        configureBanner(profile)
        configureCounts(profile)
        configureGames(profile, applications)
        configureTraits(profile)
        configureActionButton(profile)
    }

    private fun configureBanner(profile: GuildProfile) {
        banner.run {
            profile.brandColorPrimary?.let {
                val color = Integer.parseInt(it.substring(1), 16)
                setBackgroundColor(ColorUtils.setAlphaComponent(color, 255))
                aspectRatio = 5f
            }
            profile.customBannerHash?.let {
                setImageURI("https://cdn.discordapp.com/discovery-splashes/${profile.id}/${profile.customBannerHash}.png?size=1024")
                aspectRatio = 2.5f
            }
        }
    }

    private fun configureCounts(profile: GuildProfile) {
        val ctx = context ?: return
        FormatUtils.n(
            onlineCount,
            R.h.instant_invite_guild_members_online,
            arrayOf(StringUtilsKt.format(profile.onlineCount, ctx)),
            null,
            4,
        )
        FormatUtils.n(
            memberCount,
            R.h.instant_invite_guild_members_total,
            arrayOf(
                StringResourceUtilsKt.getQuantityString(
                    resources,
                    ctx,
                    Utils.getResId("instant_invite_guild_members_total_count", "plurals"),
                    profile.memberCount,
                    profile.memberCount,
                )
            ),
            null,
            4,
        )
        countsContainer.visibility = View.VISIBLE
    }

    private fun configureGames(profile: GuildProfile, applications: List<Application>) {
        val ctx = context ?: return
        games.removeAllViews()
        val size = resources.getDimension(R.d.avatar_size_standard).toInt()

        val activities = LinkedHashMap(profile.gameActivity)

        if (activities.size < 6 && activities.size != applications.size) {
            for (appId in profile.gameApplicationIds) {
                if (!activities.containsKey(appId)) {
                    activities[appId] = GuildProfile.GameActivity(0, 0)
                    if (activities.size >= 6) {
                        break
                    }
                }
            }
        }

        val sortedActivities = activities.entries
            .sortedByDescending { (_, activity) -> activity.activityScore }

        sortedActivities
            .take(5)
            .forEach { (appId, activity) ->
                val app = applications.find { it.id == appId } ?: return@forEach
                FrameLayout(ctx).addTo(games) {
                    SimpleDraweeView(ctx).addTo(this) {
                        layoutParams = FrameLayout.LayoutParams(size, size).apply {
                            topMargin = 4.dp
                            marginEnd = 4.dp
                        }
                        setImageURI("https://cdn.discordapp.com/app-icons/${app.id}/${app.c()}.png?size=256")
                        MGImages.setRoundingParams(this, 4.dp.toFloat(), false, null, null, null)
                        if (applications.size <= 5) {
                            setOnClickListener {
                                Utils.showToast(app.d())
                            }
                        }
                    }
                    if (activity.activityLevel >= 2) {
                        MaterialCardView(ctx).addTo(this) {
                            layoutParams = FrameLayout.LayoutParams(18.dp, 18.dp, Gravity.TOP or Gravity.RIGHT)
                            radius = 7.5f.dp.toFloat()
                            elevation = 0f
                            setCardBackgroundColor(ColorCompat.getThemedColor(this, R.b.colorSurface))
                            SimpleDraweeSpanTextView(ctx).addTo(this) {
                                layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.TOP or Gravity.RIGHT).apply {
                                    rightMargin = 2.dp
                                }
                                val name = StoreStream.getEmojis().unicodeEmojisNamesMap["fire"]?.surrogates
                                val emoji = MessageReactionEmoji(null, name, false)
                                EmojiNode.Companion!!.renderEmoji(this, emoji, true, 13.dp)
                            }
                        }
                    }
                }
            }
        if (sortedActivities.size > 5) {
            val app = applications.find { it.id == sortedActivities[5].key }!!
            CardView(ctx).addTo(games) {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    topMargin = 2.dp
                }
                elevation = 0f
                radius = 4.dp.toFloat()

                SimpleDraweeView(ctx).addTo(this) {
                    setImageURI("https://cdn.discordapp.com/app-icons/${app.id}/${app.c()}.png?size=256")
                    MGImages.setRoundingParams(this, 4.dp.toFloat(), false, null, null, null)
                }
                TextView(ctx).addTo(this) {
                    layoutParams = FrameLayout.LayoutParams(size, size)
                    gravity = Gravity.CENTER
                    text = "+${applications.size - 5}"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, 128))
                }
            }
        }
        if (applications.size == 1) {
            val name = applications[0].d()
            TextView(ctx, null, 0, R.i.GuildProfileSheet_DiscoverableGuild_Text).addTo(games) {
                setPadding(0, 0, 4.dp, 0)
                text = name
            }
        }
        games.visibility = if (applications.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun configureTraits(profile: GuildProfile) {
        val ctx = context ?: return
        traits.removeAllViews()
        val emojiSize = 18.dp
        for (trait in profile.traits) {
            CardView(ctx).addTo(traits) {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, emojiSize + 4.dp)
                setContentPadding(4.dp, 2.dp, 8.dp, 2.dp)
                elevation = 0f
                radius = 4.dp.toFloat()
                setCardBackgroundColor(ColorCompat.getThemedColor(this, R.b.colorBackgroundSecondary))
                LinearLayout(context).addTo(this) {
                    trait.emojiName?.let {
                        val name = if (trait.emojiId != null) {
                            it
                        } else {
                            StoreStream.getEmojis().unicodeEmojisNamesMap[it]?.surrogates
                        }
                        val emoji = MessageReactionEmoji(trait.emojiId?.toString(), name, trait.emojiAnimated)
                        SimpleDraweeSpanTextView(context).addTo(this) {
                            gravity = Gravity.CENTER
                            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, emojiSize)
                            EmojiNode.Companion!!.renderEmoji(this, emoji, true, emojiSize - 2.dp)
                        }
                    }
                    TextView(context).addTo(this) {
                        setPadding(4.dp, 0, 0, 0)
                        setTextAppearance(R.i.UiKit_TextAppearance_Semibold)
                        text = trait.label
                    }
                }
            }
        }
        traits.visibility = if (profile.traits.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun configureActionButton(profile: GuildProfile) {
        actionButton.visibility = View.VISIBLE
        actionButton.isEnabled = true
        actionButton.alpha = 1f
        val guild = StoreStream.getGuilds().getGuild(profile.id)
        // User is already in guild
        if (guild != null) {
            // User has the guild tag active
            if (StoreStream.getUsers().me.primaryGuild?.identityGuildId == profile.id) {
                actionButton.setText(R.h.hub_directory_card_joined_guild_button)
                actionButton.setOnClickListener {
                    StoreStream.getGuildSelected().set(profile.id)
                    dismiss()
                }
                // User doesn't have the guild tag active
            } else {
                actionButton.text = "Adopt Tag"
                actionButton.setOnClickListener {
                    actionButton.isEnabled = false
                    GuildTagDecorator.adoptTag(profile.id) {
                        Utils.mainThread.post { dismiss() }
                    }
                }
            }
            // Guild is publicly joinable
        } else if ("DISCOVERABLE" in profile.features) {
            actionButton.setText(R.h.join_guild)
            actionButton.setOnClickListener {
                actionButton.isEnabled = false
                // Lurking is an unfinished feature :( this will NPE upon joining + there is no code
                // to handle post-joining
                // StoreStream.getLurking().startLurkingAndNavigate(profile.id, null, null)

                GuildTagDecorator.joinGuild(requireContext(), profile.id) {
                    StoreStream.getGuildSelected().set(profile.id)
                    Utils.mainThread.post { dismiss() }
                }
            }
            // Guild requires application to join
        } else if ("MEMBER_VERIFICATION_MANUAL_APPROVAL" in profile.features) {
            actionButton.setText(R.h.guild_role_subscription_settings_enable_cta)
            actionButton.alpha = 0.5f
            actionButton.setOnClickListener {
                Utils.showToast("Server applications are not yet supported in Aliucord. Please join this server using Desktop or official Discord for now.")
            }
            // Guild is not publicly joinable
        } else {
            actionButton.visibility = View.GONE
        }
    }
}
