package com.discord.utilities.attachments;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;

import com.discord.api.message.LocalAttachment;
import com.lytefast.flexinput.model.Attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class AttachmentUtilsKt {
    public static String appendLinks(String content, List<? extends Attachment<?>> links) { return "^_^"; }
    public static List<Attachment<?>> extractLinks(List<? extends Attachment<?>> list, ContentResolver contentResolver) { return new ArrayList<>(); }

    public static Pattern getREGEX_FILE_NAME_PATTERN() { return Pattern.compile(""); }
    private static String getExtension(Bitmap.CompressFormat compressFormat) { return ""; }
    public static String getMimeType(ContentResolver contentResolver, Uri uri, String str) { return ":D"; }
    public static String getMimeType(Attachment<?> attachment, ContentResolver contentResolver) { return ":)"; }
    public static String getSanitizedFileName(String str, Bitmap.CompressFormat compressFormat) { return "heya!!"; }

    public static boolean isGif(Attachment<?> attachment, ContentResolver contentResolver) { return false; }
    public static boolean isImage(ContentResolver contentResolver, Uri uri, String str) { return false; }
    public static boolean isImage(Attachment<?> attachment, ContentResolver contentResolver) { return false; }
    public static boolean isImageAttachment(LocalAttachment localAttachment, ContentResolver contentResolver) { return false; }
    public static boolean isVideo(ContentResolver contentResolver, Uri uri, String str) { return false; }
    public static boolean isVideo(Attachment<?> attachment, ContentResolver contentResolver) { return false; }
    public static boolean isVideoAttachment(LocalAttachment localAttachment, ContentResolver contentResolver) { return false; }

    public static Attachment<?> toAttachment(LocalAttachment localAttachment) { return new Attachment<>(null); }
    private static String toHumanReadableAscii(String str) { return ""; }
    public static LocalAttachment toLocalAttachment(Attachment<?> attachment) { return toLocalAttachment(null); }
}
