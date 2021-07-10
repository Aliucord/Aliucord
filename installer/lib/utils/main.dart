/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'dart:io';

import 'package:android_intent/android_intent.dart';
import 'package:dio/dio.dart';
import 'package:filesystem_picker/filesystem_picker.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../github.dart';
export 'platform.dart';
export 'themes.dart';

final dio = Dio();
GithubAPI? githubAPI;
final storageRoot = Directory('/storage/emulated/0');
late SharedPreferences prefs;
final navigatorKey = GlobalKey<NavigatorState>();

Future<String?> pickFile(BuildContext context, String title, String ext) async => FilesystemPicker.open(
  allowedExtensions: [ ext ],
  context: context,
  fileTileSelectMode: FileTileSelectMode.wholeTile,
  fsType: FilesystemType.file,
  rootDirectory: storageRoot,
  title: title,
);

void openUrl(String url) async => await AndroidIntent(
  action: 'action_view',
  data: url,
).launch();

bool isVersionSupported(int version, String supported) => version.toString().startsWith(supported);
