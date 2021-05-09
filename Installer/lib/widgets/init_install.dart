import 'package:flutter/material.dart';

import '../pages/install.dart';
import '../utils/main.dart';

void initInstall(BuildContext context, String commit, String supportedVersion) {
  showDialog(context: context, builder: (context) => _InitInstallDialog(commit: commit, supportedVersion: supportedVersion));
}

enum _InstallOption { storage, installed_app, download }

class _InitInstallDialog extends StatefulWidget {
  final String commit;
  final String supportedVersion;

  const _InitInstallDialog({ Key? key, required this.commit, required this.supportedVersion }) : super(key: key);

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
    showDialog(context: context, builder: (context) => AlertDialog(
      title: Text('Select apk from installed app'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: discordApps.map((app) => TextButton(
          child: ListTile(
            leading: app.icon == null ? null : Image.memory(app.icon!),
            title: Text('${app.name!} (${app.packageName})'),
            subtitle: Text(
              '(ver. ${app.versionName} (${app.versionCode}))',
              style: TextStyle(color: isVersionSupported(app.versionCode, widget.supportedVersion) ? Colors.green : Colors.red)
            ),
          ),
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
        )).toList(),
      ),
      contentPadding: EdgeInsets.symmetric(vertical: 10),
    ));
  }

  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Select Discord apk'),
    content: Column(mainAxisSize: MainAxisSize.min, children: [
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
      RadioListTile(
        title: Text('From installed app ' + (_package == null ? '' : '($_package)')),
        value: _InstallOption.installed_app,
        groupValue: _option,
        onChanged: _selectFromInstalledApp,
      ),
      RadioListTile(
        title: Text('From storage ' + (_package == null && _apk != null ? '(${_apk!.split('/').last})' : '')),
        value: _InstallOption.storage,
        groupValue: _option,
        onChanged: (_InstallOption? value) async {
          final apk = await pickFile(context, 'Select Discord apk', '.apk');
          if (apk != null) setState(() {
            _option = value;
            _download = false;
            _apk = apk;
            _package = null;
          });
        },
      ),
    ]),
    actions: [
      TextButton(
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [ Icon(Icons.archive_outlined), Text(' Install') ],
        ),
        onPressed: () => Navigator.pushAndRemoveUntil(
          context, MaterialPageRoute(builder: (context) => InstallPage(apk: _apk, commit: widget.commit, download: _download, supportedVersion: widget.supportedVersion)), (r) => false),
      ),
    ],
  );
}
