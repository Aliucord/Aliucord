/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'github.dart';
import 'pages/home.dart';
import 'utils/main.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  State<MyApp> createState() => _AppState(); 
}

class _AppState extends State<MyApp> {
  bool _initialized = false;

  @override
  Widget build(BuildContext context) {
    if (!_initialized) return SizedBox.shrink();
    return MaterialApp(
      title: 'Aliucord Installer',
      theme: Themes.lightTheme,
      darkTheme: Themes.darkTheme,
      themeMode: themeManager.currentTheme(),
      home: HomePage(),
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
