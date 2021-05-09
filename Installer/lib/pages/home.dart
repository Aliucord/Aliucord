import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../constants.dart';
import '../icons.dart';
import '../widgets/init_install.dart';
import '../utils/main.dart';
import '../widgets/commits.dart';
import 'settings.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool _permissionsGranted = false;
  double? _freeSpace;
  String? _commit;
  String? _supportedVersion;
  String? _supportedVersionName;

  void _selectCommit(String? commit) async {
    if (commit == null) return;
    try {
      final dataUrl = await githubAPI!.getDownloadUrl(commit, 'data.json');
      if (dataUrl == null) return;
      final res = await dio.get(dataUrl);
      final json = jsonDecode(res.data);
      setState(() {
        _commit = commit;
        _supportedVersion = json['versionCode'];
        _supportedVersionName = json['versionName'];
      });
    } on DioError catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to get data from github:\n${e.error}')));
    }
  }

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  @override
  Widget build(BuildContext context) {
    if (!_permissionsGranted) return Scaffold(
      appBar: AppBar(title: Text('Aliucord Installer')),
      body: Center(child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            'You need to grant storage permission to use this app.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.headline6
          ),
          Padding(padding: EdgeInsets.only(top: 10), child: ElevatedButton(
            child: Text('Grant permission'),
            onPressed: _checkPermissions,
          )),
      ])),
    );

    if (_freeSpace == null) getFreeSpace().then((value) => setState(() => _freeSpace = value));

    return Scaffold(
      appBar: AppBar(
        title: Text('Aliucord Installer'),
        actions: [
          Tooltip(message: 'Support server', child: IconButton(
            icon: Icon(CustomIcons.discord),
            onPressed: () => openUrl('https://discord.gg/$supportServer'),
          )),
          PopupMenuButton<int>(
            onSelected: (action) => action == 0 ?
              Navigator.push(context, MaterialPageRoute(builder: (context) => SettingsPage())) : githubAPI!.startAuthFlow(),
            itemBuilder: (context) => [
              PopupMenuItem(child: Text('Settings'), value: 0),
              PopupMenuItem(child: Text(githubAPI!.isAuthenticated() ? 'Log out GitHub account' : 'Login with GitHub'), value: 1),
            ],
          ),
        ],
      ),
      body: Padding(padding: EdgeInsets.all(4), child: Column(children: [
        _freeSpace != null && _freeSpace! < 500 ? Card(child: ListTile(
          title: Text('You\'re running on low space'),
          subtitle: Text('Installation may fail due to not enough free space'),
          leading: Icon(Icons.data_usage, color: Colors.red, size: 40),
        )) : SizedBox.shrink(),
        Card(child: ListTile(
          title: Text('Aliucord'),
          subtitle: RichText(text: TextSpan(
            style: Theme.of(context).textTheme.bodyText2,
            text: 'Supported version: ',
            children: _supportedVersionName == null ? null : [
              TextSpan(text: _supportedVersionName, style: TextStyle(fontWeight: FontWeight.bold))
            ],
          )),
          trailing: TextButton(
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [ Icon(Icons.archive_outlined), Text(' Install') ],
            ),
            onPressed: _commit == null ? null : () => initInstall(context, _commit!, _supportedVersion!),
          ),
        )),
        Flexible(child: CommitsWidget(selectCommit: _selectCommit)),
      ])),
      bottomNavigationBar: BottomNavigationBar(
        items: [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.extension), label: 'Plugins'),
        ],
        onTap: (page) => page == 1 ? toast('Plugins page is not done yet.') : null,
      ),
    );
  }

  void _checkPermissions() {
    checkPermissions().then((res) {
      if (_permissionsGranted != res)
        setState(() => _permissionsGranted = res);
    });
  }
}
