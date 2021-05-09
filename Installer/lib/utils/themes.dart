import 'package:flutter/material.dart';

import 'main.dart';

class Themes {
  static final primaryColor = Color(0xff00c853);
  static final primaryColorLight = Color(0xff5efc82);
  static final primaryColorDark = Color(0xff009624);

  static final lightTheme = ThemeData(
    accentColor: primaryColor,
    appBarTheme: AppBarTheme(brightness: Brightness.dark, iconTheme: IconThemeData(color: Colors.white)),
    colorScheme: ColorScheme.light(primary: primaryColor, primaryVariant: primaryColorDark, onPrimary: Colors.white),
    primaryColor: primaryColor,
    primaryColorLight: primaryColorLight,
    primaryColorDark: primaryColorDark,
    primaryTextTheme: TextTheme(
      headline6: TextStyle(color: Colors.white),
    ),
    toggleableActiveColor: primaryColor,
  );
  static final darkTheme = ThemeData(
    accentColor: primaryColor,
    appBarTheme: AppBarTheme(brightness: Brightness.dark, iconTheme: IconThemeData(color: Colors.white)),
    brightness: Brightness.dark,
    colorScheme: ColorScheme.dark(primary: primaryColor, primaryVariant: primaryColorDark, onPrimary: Colors.white),
    checkboxTheme: CheckboxThemeData(fillColor: MaterialStateProperty.resolveWith((states) => primaryColor)),
    primaryColor: primaryColor,
    primaryColorLight: primaryColorLight,
    primaryColorDark: primaryColorDark,
    primaryTextTheme: TextTheme(
      headline6: TextStyle(color: Colors.white),
    ),
    toggleableActiveColor: primaryColor,
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
}

late ThemeManager themeManager;
