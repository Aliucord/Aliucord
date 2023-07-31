/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'main.dart';

class Themes {
  static const primaryColor = Color(0xff00c853);
  static const primaryColorLight = Color(0xff5efc82);
  static const primaryColorDark = Color(0xff009624);

  static final lightTheme = ThemeData(
    appBarTheme: const AppBarTheme(systemOverlayStyle: SystemUiOverlayStyle.dark, iconTheme: IconThemeData(color: Colors.black)),
    primaryColor: primaryColor,
    primaryColorLight: primaryColorLight,
    primaryColorDark: primaryColorDark,
    primaryTextTheme: const TextTheme(
      titleLarge: TextStyle(color: Colors.white),
    ),
    colorScheme: const ColorScheme.light(primary: primaryColor, primaryContainer: primaryColorDark, onPrimary: Colors.white).copyWith(secondary: primaryColor),
    useMaterial3: true,
  );
  static final darkTheme = ThemeData(
    appBarTheme: const AppBarTheme(systemOverlayStyle: SystemUiOverlayStyle.light, iconTheme: IconThemeData(color: Colors.white)),
    brightness: Brightness.dark,
    checkboxTheme: CheckboxThemeData(fillColor: MaterialStateProperty.resolveWith((states) => primaryColor)),
    primaryColor: primaryColor,
    primaryColorLight: primaryColorLight,
    primaryColorDark: primaryColorDark,
    primaryTextTheme: const TextTheme(
      titleLarge: TextStyle(color: Colors.white),
    ),
    colorScheme: const ColorScheme.dark(primary: primaryColor, primaryContainer: primaryColorDark, secondary: primaryColor, onPrimary: Colors.white).copyWith(secondary: primaryColor),
    useMaterial3: true,
  );
}

class ThemeManager with ChangeNotifier {
  int _theme = prefs.getInt('theme') ?? 0;

  ThemeMode currentTheme() => ThemeMode.values[_theme];

  void switchTheme(int theme) {
    _theme = theme;
    prefs.setInt('theme', theme);
    notifyListeners();
  }

  ThemeData applyMonet(ThemeData theme, ColorScheme? dynamic) {
    return dynamic != null ? theme.copyWith(
      appBarTheme: theme.appBarTheme.copyWith(
        iconTheme: IconThemeData(color: dynamic.secondary),
      ),
      colorScheme: dynamic,
      primaryColor: null,
      primaryColorLight: null,
      primaryColorDark: null,
      scaffoldBackgroundColor: dynamic.background,
    ) : theme;
  }
}

late ThemeManager themeManager;
