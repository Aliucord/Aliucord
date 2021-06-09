package com.discord.models.domain.emoji;

import android.content.Context;
import android.os.Parcelable;

import com.discord.models.domain.Model;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ModelEmojiUnicode {
    public static final Parcelable.Creator<ModelEmojiUnicode> CREATOR = null;

    public static class Bundle implements Model {
        public Map<EmojiCategory, List<ModelEmojiUnicode>> getEmojis() { return null; }
    }
    public ModelEmojiUnicode() { }
    public int describeContents() { return 0; }
    public List<ModelEmojiUnicode> getAsDiverse() { return null; }
    public String getChatInputText() { return null; }
    public String getCodePoints() { return null; }
    public String getCommand(String str) { return null; }
    public List<ModelEmojiUnicode> getDiversityChildren() { return null; }
    public String getFirstName() { return null; }
    public String getImageUri(boolean z2, int i, Context context) { return null; }
    public String getMessageContentReplacement() { return null; }
    public List<String> getNames() { return null; }
    public String getReactionKey() { return null; }
    public Pattern getRegex(String str) { return null; }
    public String getSurrogates() { return null; }
    public String getUniqueId() { return null; }
    public boolean isAvailable() { return true; }
    public boolean isUsable() { return true; }
    public boolean isHasDiversity() { return false; }
    public boolean isHasDiversityParent() { return false; }
    public boolean isHasMultiDiversity() { return false; }
    public boolean isHasMultiDiversityParent() { return false; }
    public static String getImageUri(String str, Context context) { return null; }
    public ModelEmojiUnicode(
            List<String> names,
            String surrogates,
            boolean hasDiversity,
            boolean hasMultiDiversity,
            boolean hasDiversityParent,
            boolean hasMultiDiversityParent,
            List<ModelEmojiUnicode> diversityChildren) { }
}
