package com.discord.models.domain.emoji;

import android.content.Context;
import android.os.Parcelable;

import com.discord.api.emoji.GuildEmoji;

import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ModelEmojiCustom {
    public static final Parcelable.Creator<ModelEmojiCustom> CREATOR = null;
    public static final int DISABLED_ALPHA = 100;
    public static final int NORMAL_ALPHA = 255;
    public static String emojiUriFormat;
    public ModelEmojiCustom() { }
    public static void setCdnUri(String str) { }
    public int describeContents() { return 0; }
    public Boolean getAvailable() { return null; }
    public String getChatInputText() { return null; }
    public String getCommand(String str) { return null; }
    public int getDisambiguationIndex() { return 0; }
    public String getFirstName() { return null; }
    public long getGuildId() { return 0; }
    public long getId() { return 0; }
    public String getIdStr() { return null; }
    public String getImageUri(boolean animated, int size, Context context) { return null; }
    public static String getImageUri(long id, boolean animated, int size) { return null; }
    public String getMessageContentReplacement() { return null; }
    public String getName() { return null; }
    public List<String> getNames() { return null; }
    public String getReactionKey() { return null; }
    public Pattern getRegex(String str) { return null; }
    public List<Long> getRoles() { return null; }
    public String getUniqueId() { return null; }
    public boolean isAnimated() { return false; }
    public boolean isAvailable() { return false; }
    public boolean isManaged() { return false; }
    public boolean isRequireColons() { return false; }
    public boolean isUsable() { return false; }
    public GuildEmoji toApiEmoji() { return null; }
    public ModelEmojiCustom(
            long id,
            String name,
            List<Long> roles,
            boolean requireColons,
            boolean managed,
            int disambiguationIndex,
            String nameDisambiguated,
            boolean isAnimated,
            boolean isUsable,
            boolean available,
            long guildId) { }

    public ModelEmojiCustom(ModelEmojiCustom modelEmojiCustom, int disambiguationIndex, boolean isUsable) { }

    public ModelEmojiCustom(GuildEmoji guildEmoji, long guildId) {
    }
}
