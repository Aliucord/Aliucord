/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.updater;

public class Updater {
    public static boolean isOutdated(String version, String newVersion) {
        if (version == null || newVersion == null) return false;
        String[] currentVersion = version.split("\\.");
        if (currentVersion.length != 3) return false;
        String[] remoteVersion = newVersion.split("\\.");
        if (remoteVersion.length != 3) return false;

        if (Integer.parseInt(remoteVersion[0]) > Integer.parseInt(currentVersion[0])) return true;
        if (remoteVersion[0].equals(currentVersion[0]) &&
                Integer.parseInt(remoteVersion[1]) > Integer.parseInt(currentVersion[1])) return true;

        return remoteVersion[0].equals(currentVersion[0]) &&
                remoteVersion[1].equals(currentVersion[1]) &&
                Integer.parseInt(remoteVersion[2]) > Integer.parseInt(currentVersion[2]);
    }
}
