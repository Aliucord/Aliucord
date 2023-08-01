/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:flutter/material.dart';

import '../pages/install.dart';
import '../utils/main.dart';

void initInstall(BuildContext context, String commit, String supportedVersion) {
  showDialog(context: context, builder: (context) => _InitInstallDialog(commit: commit, supportedVersion: supportedVersion));
}

enum _InstallOption { storage, installedApp, download }

class _InitInstallDialog extends StatefulWidget {
  final String commit;
  final String supportedVersion;

  const _InitInstallDialog({ Key? key, required this.commit, required this.supportedVersion }) : super(key: key);

  @override
  State<_InitInstallDialog> createState() => _InitInstallDialogState();
}

class _InitInstallDialogState extends State<_InitInstallDialog> {
  bool _download = true;
  String? _apk;
  String? _package;
  _InstallOption? _option = _InstallOption.download;

  bool _fetchingInstalled = false;
  void _selectFromInstalledApp(_InstallOption? value) async {
    if (_fetchingInstalled) return;
    _fetchingInstalled = true;
    final discordApps = await getInstalledDiscordApps();
    _fetchingInstalled = false;
    // ignore: use_build_context_synchronously
    showDialog(context: context, builder: (context) => AlertDialog(
      title: const Text('Select apk from installed app'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: discordApps.map((app) => TextButton(
          onPressed: () {
            Navigator.of(context, rootNavigator: true).pop();
            setState(() {
              _option = value;
              _download = false;
              _apk = app.apkPath;
              _package = app.packageName;
            });
          },
          style: TextButton.styleFrom(padding: EdgeInsets.zero),
          child: ListTile(
            leading: app.icon == null ? null : Image.memory(app.icon!),
            title: Text('${app.name!} (${app.packageName})'),
            subtitle: Text(
              '(ver. ${app.versionName} (${app.versionCode}))',
              style: TextStyle(color: isVersionSupported(app.versionCode, widget.supportedVersion) ? Colors.green : Colors.red)
            ),
          ),
        )).toList(),
      ),
      contentPadding: const EdgeInsets.symmetric(vertical: 10),
    ));
  }

  @override
  Widget build(BuildContext context) {
    final children = [
        RadioListTile(
          title: const Text('Download'),
          value: _InstallOption.download,
          groupValue: _option,
          onChanged: (_InstallOption? value) => setState(() {
            _option = value;
            _download = true;
            _apk = null;
            _package = null;
          }),
        ),
    ];

    if (prefs.getBool('developer_mode') ?? false) {
      children.add(
          RadioListTile(
            title: Text('From installed app ${_package == null ? '' : '($_package)'}'),
            value: _InstallOption.installedApp,
            groupValue: _option,
            onChanged: _selectFromInstalledApp,
          )
      );
      children.add(
          RadioListTile(
            title: Text('From storage ${_package == null && _apk != null
              ? '(${_apk!.split('/').last})'
              : ''}'),
            value: _InstallOption.storage,
            groupValue: _option,
            onChanged: (_InstallOption? value) async {
              final apk = await pickFile(context, 'Select Discord apk', '.apk');
              if (apk != null) {
                setState(() {
                _option = value;
                _download = false;
                _apk = apk;
                _package = null;
              });
              }
            },
          )
      );
    }

    return AlertDialog(
      title: const Text('Select Discord apk'),
      content: Column(mainAxisSize: MainAxisSize.min, children: children),
      actions: [
        TextButton(
          child: const Row(
            mainAxisSize: MainAxisSize.min,
            children: [ Icon(Icons.archive_outlined), Text(' Install') ],
          ),
          onPressed: () => Navigator.pushAndRemoveUntil(
            context, MaterialPageRoute(builder: (context) => InstallPage(apk: _apk, commit: widget.commit, download: _download, supportedVersion: widget.supportedVersion)), (r) => false),
        ),
      ],
    );
  }
}
