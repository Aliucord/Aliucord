/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/services.dart';

const channel = MethodChannel('main');

class AppInfo {
  String apkPath;
  String packageName;
  String? name;
  int versionCode;
  String versionName;
  Uint8List? icon;

  AppInfo({ required this.apkPath, required this.packageName, this.name, required this.versionCode, required this.versionName, this.icon });

  AppInfo.fromMap(dynamic map)
    : apkPath = map['apkPath'],
      packageName = map['packageName'],
      name = map['name'],
      versionCode = map['versionCode'] ?? 0,
      versionName = map['versionName'] ?? 'unknown',
      icon = map['icon'] == null ? null : base64.decode(map['icon']);
}

Future<bool> checkPermissions() async => await channel.invokeMethod('checkPermissions');
Future<double> getFreeSpace() async => await channel.invokeMethod('getFreeSpace');
Future<int> getVersionCode() async => await channel.invokeMethod('getVersionCode');
Future<String> getVersionName() async => await channel.invokeMethod('getVersionName');
Future<void> toast(String message) => channel.invokeMethod('toast', message);

Future<Iterable<AppInfo>> getInstalledDiscordApps() async {
  List<dynamic> apps = await channel.invokeMethod('getInstalledDiscordApps');
  return apps.map((e) => AppInfo.fromMap(e));
}
Future<AppInfo?> getApkInfo(String path) async {
  final info = await channel.invokeMethod('getApkInfo', path);
  return info == null ? null : AppInfo.fromMap(info);
}

Future<void> patchApk(String path, bool replaceBg) =>
  channel.invokeMethod('patchApk', { 'path': path, 'replaceBg': replaceBg });
Future<void> signApk() => channel.invokeMethod('signApk');
Future<void> installApk(String path) => channel.invokeMethod('installApk', path);
