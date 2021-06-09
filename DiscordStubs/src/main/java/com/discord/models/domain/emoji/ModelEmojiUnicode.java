package com.discord.models.domain.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import c.a.m.a.u0.a;
import c.a.m.a.u0.b;
import c.a.m.a.u0.c;
import com.discord.models.domain.Model;
import com.discord.widgets.chat.input.MentionUtilsKt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ModelEmojiUnicode implements Model, Emoji {
    public static final Parcelable.Creator<ModelEmojiUnicode> CREATOR = new Parcelable.Creator<ModelEmojiUnicode>() {
        /* class com.discord.models.domain.emoji.ModelEmojiUnicode.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ModelEmojiUnicode createFromParcel(Parcel parcel) {
            ArrayList arrayList = new ArrayList();
            parcel.readStringList(arrayList);
            String readString = parcel.readString();
            boolean z2 = parcel.readInt() > 0;
            boolean z3 = parcel.readInt() > 0;
            boolean z4 = parcel.readInt() > 0;
            boolean z5 = parcel.readInt() > 0;
            ArrayList arrayList2 = new ArrayList();
            parcel.readTypedList(arrayList2, this);
            return new ModelEmojiUnicode(arrayList, readString, z2, z3, z4, z5, arrayList2);
        }

        @Override // android.os.Parcelable.Creator
        public ModelEmojiUnicode[] newArray(int i) {
            return new ModelEmojiUnicode[i];
        }
    };
    private static final String[] DIVERSITY_MODIFIERS = {"🏻", "🏼", "🏽", "🏾", "🏿"};
    private static final String FILENAME_FORMAT = "emoji_%s";
    private static final String SKIN_TONE_SUFFIX = "::skin-tone-";
    private static final String URI_FORMAT = "res:///%d";
    private final AtomicReference<Object> codePoints = new AtomicReference<>();
    private List<ModelEmojiUnicode> diversityChildren;
    private boolean hasDiversity;
    private boolean hasDiversityParent;
    private boolean hasMultiDiversity;
    private boolean hasMultiDiversityParent;
    private List<String> names;
    private String surrogates;

    public static class Bundle implements Model {
        private Map<EmojiCategory, List<ModelEmojiUnicode>> emojis = new HashMap();

        @Override // com.discord.models.domain.Model
        public void assignField(Model.JsonReader jsonReader) throws IOException {
            EmojiCategory byString = EmojiCategory.getByString(jsonReader.nextName());
            if (byString != null) {
                this.emojis.put(byString, jsonReader.nextList(new a(jsonReader)));
            } else {
                jsonReader.skipValue();
            }
        }

        public boolean canEqual(Object obj) {
            return obj instanceof Bundle;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Bundle)) {
                return false;
            }
            Bundle bundle = (Bundle) obj;
            if (!bundle.canEqual(this)) {
                return false;
            }
            Map<EmojiCategory, List<ModelEmojiUnicode>> emojis2 = getEmojis();
            Map<EmojiCategory, List<ModelEmojiUnicode>> emojis3 = bundle.getEmojis();
            return emojis2 != null ? emojis2.equals(emojis3) : emojis3 == null;
        }

        public Map<EmojiCategory, List<ModelEmojiUnicode>> getEmojis() {
            return this.emojis;
        }

        public int hashCode() {
            Map<EmojiCategory, List<ModelEmojiUnicode>> emojis2 = getEmojis();
            return 59 + (emojis2 == null ? 43 : emojis2.hashCode());
        }

        public String toString() {
            StringBuilder L = c.d.b.a.a.L("ModelEmojiUnicode.Bundle(emojis=");
            L.append(getEmojis());
            L.append(")");
            return L.toString();
        }
    }

    public ModelEmojiUnicode() {
    }

    private String toCodePoint() {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (true) {
            char c2 = 0;
            while (i < this.surrogates.length()) {
                int i2 = i + 1;
                char charAt = this.surrogates.charAt(i);
                if (c2 != 0) {
                    arrayList.add(Integer.toHexString((charAt - 56320) + ((c2 - 55296) << 10) + 65536));
                    i = i2;
                } else {
                    if (55296 > charAt || charAt > 56319) {
                        arrayList.add(Integer.toHexString(charAt));
                    } else {
                        c2 = charAt;
                    }
                    i = i2;
                }
            }
            return TextUtils.join("_", arrayList);
        }
    }

    @Override // com.discord.models.domain.Model
    public void assignField(Model.JsonReader jsonReader) throws IOException {
        String nextName = jsonReader.nextName();
        nextName.hashCode();
        char c2 = 65535;
        switch (nextName.hashCode()) {
            case -2019572390:
                if (nextName.equals("hasMultiDiversity")) {
                    c2 = 0;
                    break;
                }
                break;
            case -1212936855:
                if (nextName.equals("hasDiversityParent")) {
                    c2 = 1;
                    break;
                }
                break;
            case 104585032:
                if (nextName.equals("names")) {
                    c2 = 2;
                    break;
                }
                break;
            case 341058715:
                if (nextName.equals("surrogates")) {
                    c2 = 3;
                    break;
                }
                break;
            case 1515109343:
                if (nextName.equals("hasDiversity")) {
                    c2 = 4;
                    break;
                }
                break;
            case 1727581860:
                if (nextName.equals("hasMultiDiversityParent")) {
                    c2 = 5;
                    break;
                }
                break;
            case 2016033400:
                if (nextName.equals("diversityChildren")) {
                    c2 = 6;
                    break;
                }
                break;
        }
        switch (c2) {
            case 0:
                this.hasMultiDiversity = jsonReader.nextBoolean(this.hasMultiDiversity);
                return;
            case 1:
                this.hasDiversityParent = jsonReader.nextBoolean(this.hasDiversityParent);
                return;
            case 2:
                this.names = jsonReader.nextList(new c(jsonReader));
                return;
            case 3:
                this.surrogates = jsonReader.nextString(this.surrogates);
                return;
            case 4:
                this.hasDiversity = jsonReader.nextBoolean(this.hasDiversity);
                return;
            case 5:
                this.hasMultiDiversityParent = jsonReader.nextBoolean(this.hasMultiDiversityParent);
                return;
            case 6:
                this.diversityChildren = jsonReader.nextList(new b(jsonReader));
                return;
            default:
                jsonReader.skipValue();
                return;
        }
    }

    public boolean canEqual(Object obj) {
        return obj instanceof ModelEmojiUnicode;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModelEmojiUnicode)) {
            return false;
        }
        ModelEmojiUnicode modelEmojiUnicode = (ModelEmojiUnicode) obj;
        if (!modelEmojiUnicode.canEqual(this) || isHasDiversity() != modelEmojiUnicode.isHasDiversity() || isHasMultiDiversity() != modelEmojiUnicode.isHasMultiDiversity() || isHasDiversityParent() != modelEmojiUnicode.isHasDiversityParent() || isHasMultiDiversityParent() != modelEmojiUnicode.isHasMultiDiversityParent()) {
            return false;
        }
        List<String> names2 = getNames();
        List<String> names3 = modelEmojiUnicode.getNames();
        if (names2 != null ? !names2.equals(names3) : names3 != null) {
            return false;
        }
        String surrogates2 = getSurrogates();
        String surrogates3 = modelEmojiUnicode.getSurrogates();
        if (surrogates2 != null ? !surrogates2.equals(surrogates3) : surrogates3 != null) {
            return false;
        }
        List<ModelEmojiUnicode> diversityChildren2 = getDiversityChildren();
        List<ModelEmojiUnicode> diversityChildren3 = modelEmojiUnicode.getDiversityChildren();
        if (diversityChildren2 != null ? !diversityChildren2.equals(diversityChildren3) : diversityChildren3 != null) {
            return false;
        }
        String codePoints2 = getCodePoints();
        String codePoints3 = modelEmojiUnicode.getCodePoints();
        return codePoints2 != null ? codePoints2.equals(codePoints3) : codePoints3 == null;
    }

    public List<ModelEmojiUnicode> getAsDiverse() {
        List<ModelEmojiUnicode> list = this.diversityChildren;
        return list == null ? Collections.emptyList() : list;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getChatInputText() {
        return getCommand(getFirstName());
    }

    public String getCodePoints() {
        Object obj = this.codePoints.get();
        if (obj == null) {
            synchronized (this.codePoints) {
                obj = this.codePoints.get();
                if (obj == null) {
                    obj = toCodePoint();
                    if (obj == null) {
                        obj = this.codePoints;
                    }
                    this.codePoints.set(obj);
                }
            }
        }
        if (obj == this.codePoints) {
            obj = null;
        }
        return (String) obj;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getCommand(String str) {
        if (str == null) {
            str = getFirstName();
        }
        return String.format(":%s:", str);
    }

    public List<ModelEmojiUnicode> getDiversityChildren() {
        return this.diversityChildren;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getFirstName() {
        return this.names.get(0);
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getImageUri(boolean z2, int i, Context context) {
        return getImageUri(getCodePoints(), context);
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getMessageContentReplacement() {
        return this.surrogates;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public List<String> getNames() {
        return this.names;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getReactionKey() {
        return this.surrogates;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public Pattern getRegex(String str) {
        if (str == null) {
            str = getFirstName();
        }
        try {
            return Pattern.compile("([^\\\\]|^):" + str + MentionUtilsKt.EMOJIS_AND_STICKERS_CHAR);
        } catch (PatternSyntaxException unused) {
            return Pattern.compile("$^");
        }
    }

    public String getSurrogates() {
        return this.surrogates;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public String getUniqueId() {
        return this.surrogates;
    }

    public int hashCode() {
        int i = 79;
        int i2 = ((((((isHasDiversity() ? 79 : 97) + 59) * 59) + (isHasMultiDiversity() ? 79 : 97)) * 59) + (isHasDiversityParent() ? 79 : 97)) * 59;
        if (!isHasMultiDiversityParent()) {
            i = 97;
        }
        int i3 = i2 + i;
        List<String> names2 = getNames();
        int i4 = 43;
        int hashCode = (i3 * 59) + (names2 == null ? 43 : names2.hashCode());
        String surrogates2 = getSurrogates();
        int hashCode2 = (hashCode * 59) + (surrogates2 == null ? 43 : surrogates2.hashCode());
        List<ModelEmojiUnicode> diversityChildren2 = getDiversityChildren();
        int hashCode3 = (hashCode2 * 59) + (diversityChildren2 == null ? 43 : diversityChildren2.hashCode());
        String codePoints2 = getCodePoints();
        int i5 = hashCode3 * 59;
        if (codePoints2 != null) {
            i4 = codePoints2.hashCode();
        }
        return i5 + i4;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public boolean isAvailable() {
        return true;
    }

    public boolean isHasDiversity() {
        return this.hasDiversity;
    }

    public boolean isHasDiversityParent() {
        return this.hasDiversityParent;
    }

    public boolean isHasMultiDiversity() {
        return this.hasMultiDiversity;
    }

    public boolean isHasMultiDiversityParent() {
        return this.hasMultiDiversityParent;
    }

    @Override // com.discord.models.domain.emoji.Emoji
    public boolean isUsable() {
        return true;
    }

    public String toString() {
        StringBuilder L = c.d.b.a.a.L("ModelEmojiUnicode(names=");
        L.append(getNames());
        L.append(", surrogates=");
        L.append(getSurrogates());
        L.append(", hasDiversity=");
        L.append(isHasDiversity());
        L.append(", hasMultiDiversity=");
        L.append(isHasMultiDiversity());
        L.append(", hasDiversityParent=");
        L.append(isHasDiversityParent());
        L.append(", hasMultiDiversityParent=");
        L.append(isHasMultiDiversityParent());
        L.append(", diversityChildren=");
        L.append(getDiversityChildren());
        L.append(", codePoints=");
        L.append(getCodePoints());
        L.append(")");
        return L.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(this.names);
        parcel.writeString(this.surrogates);
        parcel.writeInt(this.hasDiversity ? 1 : 0);
        parcel.writeInt(this.hasMultiDiversity ? 1 : 0);
        parcel.writeInt(this.hasDiversityParent ? 1 : 0);
        parcel.writeInt(this.hasMultiDiversityParent ? 1 : 0);
        parcel.writeTypedList(this.diversityChildren);
    }

    @SuppressLint({"DefaultLocale"})
    public static String getImageUri(String str, Context context) {
        return String.format(URI_FORMAT, Integer.valueOf(context.getResources().getIdentifier(String.format(FILENAME_FORMAT, str), "raw", context.getPackageName())));
    }

    public ModelEmojiUnicode(List<String> list, String str, boolean z2, boolean z3, boolean z4, boolean z5, List<ModelEmojiUnicode> list2) {
        this.names = list;
        this.surrogates = str;
        this.hasDiversity = z2;
        this.hasMultiDiversity = z3;
        this.hasDiversityParent = z4;
        this.hasMultiDiversityParent = z5;
        this.diversityChildren = list2;
    }
}
