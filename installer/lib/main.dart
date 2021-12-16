/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'dart:io';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'github.dart';
import 'pages/home.dart';
import 'utils/main.dart';

class _HttpOverrides extends HttpOverrides {
  @override
  HttpClient createHttpClient(SecurityContext? context) =>
    super.createHttpClient(context)
      ..badCertificateCallback = (cert, host, port) => true;
}

void main() {
  HttpOverrides.global = _HttpOverrides();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _AppState();
}

class _AppState extends State<MyApp> {
  bool _initialized = false;

  @override
  Widget build(BuildContext context) {
    if (!_initialized) return const SizedBox.shrink();
    return MaterialApp(
      title: 'Aliucord Installer',
      theme: Themes.lightTheme,
      darkTheme: Themes.darkTheme,
      themeMode: themeManager.currentTheme(),
      home: const HomePage(),
      navigatorKey: navigatorKey,
    );
  }

  @override
  void initState() {
    super.initState();

    SharedPreferences.getInstance().then((value) {
      prefs = value;
      themeManager = ThemeManager();
      themeManager.addListener(() => setState(() {}));
      githubAPI = GithubAPI();
      setState(() => _initialized = true);
    });
  }
}
