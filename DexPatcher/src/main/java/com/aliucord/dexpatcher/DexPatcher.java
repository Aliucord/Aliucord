package com.aliucord.dexpatcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.aliucord.libzip.Zip;
import com.google.gson.Gson;

import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DexPatcher {
    public static final Pattern methodRegex = Pattern.compile("\\.method (.*?) ([\\w|<|>|$]+\\(.*?\\))(.*?)\n(.*?)\n\\.end method", Pattern.DOTALL);
    // TODO: ability to patch constructor
    public static final String[] ignoredMethods = new String[]{"abstract", "bridge", "constructor", "native"};

    Context context;
    Map<String, String> formats = new HashMap<>();
    StateUpdater stateUpdater;
    DexPatcherOptions options;

    @SuppressWarnings("unused")
    public DexPatcher(Context ctx, @Nullable StateUpdater updater) throws Exception {
        this(ctx, updater, new DexPatcherOptions());
    }

    public DexPatcher(Context ctx, @Nullable StateUpdater updater, DexPatcherOptions options) throws Exception {
        context = ctx;
        this.options = options;
        readAssets(ctx.getAssets(), "dexpatcher");
        if (updater == null) stateUpdater = state -> Log.d("Aliucord DexPatcher", state);
        else stateUpdater = updater;
    }

    private void readAssets(AssetManager assets, String path) throws Exception {
        String[] list = assets.list(path);
        if (list.length == 0) formats.put(path.replace("dexpatcher/", "").split("\\.")[0], Utils.readFile(assets.open(path)));
        else for (String p : list) readAssets(assets, path + "/" + p);
    }

    public void patchApk(String path, Map<String, List<String>> classes, String outApk, boolean firstPatch, File aliucordDex) throws Exception {
        stateUpdater.update("Patching apk: " + path);
        File cachePath = new File(context.getCacheDir(), "patcher");

        if (options.clearCache) Utils.delete(cachePath);

        File classesDex = new File(cachePath, "classes2.dex");
        File classesDex2 = new File(cachePath, "classes3.dex");
        File classesDex3 = new File(cachePath, "classes4.dex");
        File smaliClasses = new File(cachePath, "classes2");
        File smaliClasses2 = new File(cachePath, "classes3");
        File smaliClasses3 = new File(cachePath, "classes4");

        File[] files = cachePath.listFiles((d, name) -> name.endsWith(".dex"));
        if (files == null || files.length < 3) {
            stateUpdater.update("Unpacking classes from apk");
            Utils.unzipClasses(context, path);
            files = Objects.requireNonNull(cachePath.listFiles((d, name) -> name.endsWith(".dex")));
        }

        if (files.length == 3) {
            classesDex2.renameTo(classesDex3);
            classesDex.renameTo(classesDex2);
            (new File(cachePath, "classes.dex")).renameTo(classesDex);
        }

        if (!smaliClasses.exists() || !smaliClasses2.exists()) {
            stateUpdater.update("Baksmaling 1st classes.dex");
            Utils.baksmaliDex(context, classesDex);
            stateUpdater.update("Baksmaling 2nd classes.dex");
            Utils.baksmaliDex(context, classesDex2);
            stateUpdater.update("Baksmaling 3rd classes.dex");
            Utils.baksmaliDex(context, classesDex3);
        }

        File out = new File(cachePath, "out");
        if (out.exists()) Utils.delete(out);
        out.mkdirs();

        Map<File, List<String>> filesToPatch = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : classes.entrySet()) {
            String c = entry.getKey().replaceAll("\\.", "/") + ".smali";
            File src = new File(smaliClasses, c);
            if (!src.exists()) src = new File(smaliClasses2, c);
            if (!src.exists()) src = new File(smaliClasses3, c);
            if (!src.exists()) {
                Log.w("Aliucord DexPatcher", "File not found: " + c);
                continue;
            }
            File dest = new File(out, c);
            if (!Objects.requireNonNull(dest.getParentFile()).exists()) dest.getParentFile().mkdirs();
            Utils.copyFile(src, dest);
            filesToPatch.put(dest, entry.getValue());
        }

        for (Map.Entry<File, List<String>> entry : filesToPatch.entrySet()) patchFile(entry.getKey(), entry.getValue());

        stateUpdater.update("Smaling patched classes.dex");
        File outDex = new File(cachePath, "classes.dex");
        SmaliOptions options = new SmaliOptions();
        options.outputDexFile = outDex.getAbsolutePath();
        Smali.assemble(options, out.getAbsolutePath());

        stateUpdater.update("Copying original apk");
        File outApkFile = new File(outApk);
        Utils.copyFile(new File(path), outApkFile);

        List<File> filesToAdd = new ArrayList<File>(){{ add(outDex); add(classesDex); add(classesDex2); add(classesDex3); add(aliucordDex); }};
        if (firstPatch) {
            File manifest = new File(cachePath, "AndroidManifest.xml");
            Utils.copyAsset(context.getAssets().open("AndroidManifest.xml"), manifest);
            filesToAdd.add(manifest);
        }

        stateUpdater.update("Repacking new apk with new classes");
        Zip zip = new Zip(outApk, 6, 'a');
        for (File fileToAdd : filesToAdd) zip.deleteEntry(fileToAdd.getName());
        zip.deleteEntry("assets/aliucord-patches.json");
        for (File fileToAdd : filesToAdd) {
            zip.openEntry(fileToAdd.getName());
            zip.compressFile(fileToAdd.getAbsolutePath());
            zip.closeEntry();
        }
        zip.openEntry("assets/aliucord-patches.json");
        byte[] json = new Gson().toJson(new HashMap<>(classes)).getBytes();
        zip.writeEntry(json, json.length);
        zip.closeEntry();
        zip.close();

        if (this.options.newBg != null) {
            byte[] bytes = Utils.readBytes(this.options.newBg);

            // List<String> bgEntries = new ArrayList<>();
            // String[] folders = new String[]{ "mipmap-hdpi-v4", "mipmap-xhdpi-v4", "mipmap-xxhdpi-v4", "mipmap-xxxhdpi-v4" };
            // for (String folder : folders) {
            //     for (String fileName : new String[]{ "ic_launcher_background.png", "ic_logo_background.png" }) {
            //         bgEntries.add("res/" + folder + "/" + fileName);
            //     }
            // }
            String[] bgEntries = new String[]{ "res/ikM.png", "res/63Y.png", "res/B1u.png", "res/DF6.png" };

            zip = new Zip(outApk, 0, 'a');
            for (String entryName : bgEntries) zip.deleteEntry(entryName);
            for (String entryName : bgEntries) {
                zip.openEntry(entryName);
                zip.writeEntry(bytes, bytes.length);
                zip.closeEntry();
            }
            zip.close();
        }
    }

    public void patchFile(File file, List<String> methodsToPatch) {
        try {
            stateUpdater.update("Patching file: " + file.getAbsolutePath());
            String s = Utils.readFile(file);
            String[] sArr = s.split("\n")[0].split(" ");
            String className = sArr[sArr.length - 1];
            Matcher matcher = methodRegex.matcher(s);

            while (matcher.find()) {
                boolean skip = false;
                String g1 = Objects.requireNonNull(matcher.group(1));
                for (String i : ignoredMethods) {
                    if (g1.contains(i)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) continue;

                String g2 = Objects.requireNonNull(matcher.group(2));
                String[] g2Arr = g2.split("\\(");
                if (!methodsToPatch.contains("*") && !methodsToPatch.contains(g2Arr[0])) continue;

                boolean _static = false;
                String _this = "move-object/from16 v3, p0";
                String invoke = "virtual";
                if (g1.contains("static")) {
                    _static = true;
                    _this = "const/4 v3, 0x0";
                    invoke = "static";
                } else if (g1.contains("private")) invoke = "direct";

                StringBuilder addArgs = new StringBuilder();
                int argI = _static ? 0 : 1;
                boolean objectStarted = false;
                boolean skipNext = false;
                String args = g2Arr[1];
                for (int i = 0; i < args.length(); i++) {
                    char type = args.charAt(i);
                    if (skipNext) {
                        skipNext = false;
                        continue;
                    }
                    if (objectStarted) {
                        if (type == ';') objectStarted = false;
                        continue;
                    }
                    if (!Character.isLetter(type) && type != '[') continue;

                    if (type == '[') {
                        if (args.charAt(i + 1) == 'L') objectStarted = true;
                        else skipNext = true;
                    } else if (type == 'L') objectStarted = true;

                    addArgs.append(addArgToArray(argI, type));
                    if (type == 'J' || type == 'D') argI++;
                    argI++;
                }

                String g3 = Objects.requireNonNull(matcher.group(3));
                char retType = g3.charAt(0);
                if (retType == '[') retType = 'L';

                String ret = getVal(g3, "v1") + formats.get(retType + "/ret");

                StringBuilder getArgs = new StringBuilder();
                argI = 0;
                skipNext = false;
                StringBuilder tmpObj = new StringBuilder();
                int _i = 0;
                for (int i = 0; i < args.length(); i++) {
                    char type = args.charAt(i);
                    if (skipNext) {
                        skipNext = false;
                        continue;
                    }
                    if (objectStarted) {
                        tmpObj.append(type);
                        if (type != ';') continue;
                    }
                    if (!Character.isLetter(type) && type != ';' && type != '[') continue;

                    String _type = String.valueOf(type);
                    if (type == '[') {
                        if (args.charAt(i + 1) == 'L') {
                            tmpObj = new StringBuilder();
                            tmpObj.append(type);
                            objectStarted = true;
                            continue;
                        }
                        _type += args.charAt(i + 1);
                        skipNext = true;
                    } else if (type == 'L') {
                        tmpObj = new StringBuilder();
                        tmpObj.append("L");
                        objectStarted = true;
                        continue;
                    }

                    if (objectStarted) {
                        _type = tmpObj.toString();
                        objectStarted = false;
                    }

                    getArgs.append(getArg(argI, _i, _type, _static));
                    if (type == 'J' || type == 'D') argI++;
                    argI++;
                    _i++;
                }

                String invokeArgs = "";
                if (argI != 0) {
                    invoke += "/range";
                    invokeArgs = "p0 .. p" + (_static ? argI - 1 : argI);
                } else if (!_static) invokeArgs = "p0";

                String newMethodName = g2Arr[0] + "$__aliucordOriginal(" + args;

                String moveResult = "const/4 v1, 0x0";
                if (!g3.equals("V")) {
                    moveResult = "move-result";
                    char type = g3.charAt(0);
                    switch (type) {
                        case 'L':
                        case '[':
                            moveResult += "-object";
                            break;
                        case 'D':
                        case 'J':
                            moveResult += "-wide";
                            break;
                    }
                    moveResult += " v1";
                    if (type != 'L' && type != '[') {
                        moveResult += "\n\n" + toObj(type, "v1", type == 'D' || type == 'J' ? "v2" : "v1", "v1");
                    }
                }

                String newCode = String.format(formats.get("patchedmethod"), g1, g2, g3, addArgs.toString(), _this, ret, getArgs.toString(),
                        invoke, invokeArgs, className, newMethodName, g3, moveResult, ret);
                s = s.replace(matcher.group(),
                        newCode + "\n\n.method " + g1 + " " + newMethodName + g3 + "\n" + matcher.group(4) + "\n.end method");
            }
            OutputStream out = new FileOutputStream(file);
            out.write(s.getBytes());
            out.close();
        } catch (Exception e) {
            Log.e("Aliucord DexPatcher", "Failed to patch file!", e);
        }
    }

    private String toObj(char type, String v1, String v2, String ret) {
        if (type == 'V') return "";
        if (type == 'L') return String.format(formats.get("L/obj"), ret, v1);
        return String.format(formats.get(type + "/obj"), v1, v2, ret);
    }

    private String addArgToArray(int index, char type) {
        if (type == '[') type = 'L';
        return toObj(type, "p" + index, "p" + (type == 'D' || type == 'J' ? index + 1 : index), "v2")
                + "\ninvoke-virtual {v0, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z\n";
    }

    private String getVal(String type, String v) {
        if (type.equals("V")) return "";
        if (type.length() > 1) return String.format(formats.get("L/val"), type, v);
        return String.format(formats.get(type + "/val"), v);
    }

    private String getArg(int index, int i, String type, boolean _static) {
        String v = _static ? "p" + index : "p" + (index + 1);
        return String.format(formats.get("getarg"), Integer.toHexString(i), getVal(type, v));
    }
}
