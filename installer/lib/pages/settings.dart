/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

import '../constants.dart';
import '../utils/main.dart';

class CheckBoxData {
  String key;
  String label;
  bool defaultValue;
  bool? value;

  CheckBoxData(this.key, this.label, this.defaultValue);
}

class SettingsPage extends StatefulWidget {
  const SettingsPage({Key? key}) : super(key: key);

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> with SingleTickerProviderStateMixin {
  static final List<CheckBoxData> _checkBoxes = [
    CheckBoxData('replace_bg', 'Replace icon background with Aliucord\'s', true),
    CheckBoxData('use_dex_from_storage', 'Use Injector.dex from storage', false),
    CheckBoxData('developer_mode', 'Developer Mode', false),
  ];
  int _theme = 0;
  String _dexLocation = defaultDexLocation;
  bool _loaded = false;

  @override
  Widget build(BuildContext context) {
    if (!_loaded) {
      _theme = prefs.getInt('theme') ?? _theme;
      _dexLocation = prefs.getString('dex_location') ?? _dexLocation;
      for (var data in _checkBoxes) {
        data.value = prefs.getBool(data.key) ?? data.defaultValue;
      }
      _loaded = true;
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          ListTile(
            title: const Text('Theme'),
            trailing: DropdownButton(
              value: _theme,
              items: const [
                DropdownMenuItem(value: 0, child: Padding(padding: EdgeInsets.only(right: 70), child: Text('System'))),
                DropdownMenuItem(value: 1, child: Text('Light')),
                DropdownMenuItem(value: 2, child: Text('Dark')),
              ],
              onChanged: (newValue) {
                themeManager.switchTheme(newValue as int);
                setState(() => _theme = newValue);
              },
            ),
          ),
          ...buildCheckBoxes(),
          ListTile(
            title: const Text('Dex location'),
            subtitle: Text(_dexLocation),
            enabled: prefs.getBool('use_dex_from_storage') ?? false,
            onTap: () async {
              final path = await pickFile(context, 'Select Injector.dex', '.dex');
              if (path != null) {
                prefs.setString('dex_location', path);
                setState(() => _dexLocation = path);
              }
            },
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            child: ElevatedButton(
              onPressed: () async {
                final files = (await getApplicationSupportDirectory()).listSync();
                for (final file in files) {
                  file.delete(recursive: true);
                }
                prefs.remove('dex_commit');
              },
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(7),
              ),
              child: const Text('Clear files cache (Injector.dex and patched manifest)', textAlign: TextAlign.center),
            ),
          ),
        ],
      ),
    );
  }

  Iterable<Widget> buildCheckBoxes() {
    return _checkBoxes.map((data) => CheckboxListTile(
      title: Text(data.label),
      value: data.value,
      onChanged: (newValue) {
        prefs.setBool(data.key, newValue as bool);
        setState(() => data.value = newValue);
      }
    ));
  }
}
