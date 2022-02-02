/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

import '../constants.dart';
import '../utils/main.dart';
import '../widgets/logs.dart';
import 'home.dart';

class InstallPage extends StatefulWidget {
  final String? apk;
  final String commit;
  final bool download;
  final String supportedVersion;

  const InstallPage({
    Key? key,
    required this.apk,
    required this.commit,
    required this.download,
    required this.supportedVersion
  }) : super(key: key);

  @override
  State<InstallPage> createState() => _InstallPageState();
}

class _InstallPageState extends State<InstallPage> {
  bool _allowBack = false;
  bool _progressBar = true;
  double? _progress;
  String _logs = '';

  void _downloadDiscord(String apk) async {
    setState(() => _logs += 'Downloading discord apk..\n');
    try {
      await dio.download(
        '$backendHost/download/discord?v=${widget.supportedVersion}',
        apk,
        onReceiveProgress: (count, total) => setState(() => _progress = count / total),
      );
      setState(() {
        _progress = null;
        _logs += 'Done\n';
      });
      _install(apk);
    } on DioError catch (e) {
      _onFailed();
      setState(() =>
        _logs += '\nAn exception occurred while downloading Discord. Please try again. If this error persists, try a different network.\n${e.error}\n'
      );
    }
  }

  void _install(String apk) async {
    final cache = (await getApplicationSupportDirectory()).path;
    final aliucordDex = '$cache/classes.dex';
    var downloadManifest = true;
    if (prefs.getBool('use_dex_from_storage') ?? false) {
      final dexFile = File(prefs.getString('dex_location') ?? defaultDexLocation);
      if (await dexFile.exists()) {
        await dexFile.copy(aliucordDex);
        final manifestFile = File(storageRoot.path + '/Aliucord/AndroidManifest.xml');
        if (await manifestFile.exists()) {
          await manifestFile.copy('$cache/AndroidManifest.xml');
          downloadManifest = false;
        }
        if (prefs.containsKey('dex_commit')) prefs.remove('dex_commit'); // invalidate cache
      } else if (!await _downloadAliucord(aliucordDex)) {
        return _onFailed();
      }
    } else if (!await _downloadAliucord(aliucordDex)) {
      return _onFailed();
    }
    if (downloadManifest && !await _downloadManifest(cache)) return _onFailed();

    const updater = MethodChannel('updater');
    updater.setMethodCallHandler((call) async => setState(() => _logs += call.arguments + '\n'));
    try {
      await patchApk(apk, prefs.getBool('replace_bg') ?? true);
      await signApk();
      installApk(storageRoot.path + '/Aliucord/Aliucord.apk');
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const HomePage()));
    } on PlatformException catch (e) {
      _onFailed();
      setState(() => _logs += '${e.message}${e.details}\n');
    }
  }

  Future<bool> _downloadAliucord(String out) async {
    if ((prefs.getString('dex_commit') ?? '') == widget.commit && await File(out).exists()) return true;
    setState(() => _logs += 'Downloading Injector.dex..\n');
    final url = githubAPI!.getDownloadUrl(widget.commit, 'Injector.dex');
    try {
      await dio.download(url, out, onReceiveProgress: (count, total) => setState(() => _progress = count / total));
      setState(() {
        _progress = null;
        _logs += 'Done\n';
      });
      return true;
    } on DioError catch (e) {
      _onFailed();
      setState(() => _logs += '${e.error}\n');
    }
    return false;
  }

  Future<bool> _downloadManifest(String cache) async {
    final manifest = '$cache/AndroidManifest.xml';
    setState(() => _logs += 'Downloading patched AndroidManifest.xml..\n');
    try {
      await dio.download(
        githubAPI!.getDownloadUrl('builds', 'AndroidManifest.xml'),
        manifest,
        onReceiveProgress: (count, total) => setState(() => _progress = count / total),
      );
      setState(() {
        _progress = null;
        _logs += 'Done\n';
      });
      return true;
    } on DioError catch (e) {
      _onFailed();
      setState(() => _logs += '${e.error}\n');
    }
    return false;
  }

  void _onFailed() => setState(() {
    _allowBack = true;
    _progressBar = false;
  });

  @override
  void initState() {
    super.initState();
    if (widget.download) {
      _logs += 'Checking cached apk..\n';
      getTemporaryDirectory().then((cache) async {
        final cachedApk = cache.path + '/discord.apk';
        if (!await File(cachedApk).exists()) return _downloadDiscord(cachedApk);
        final info = await getApkInfo(cachedApk);
        if (info == null || !isVersionSupported(info.versionCode, widget.supportedVersion)) {
          _downloadDiscord(cachedApk);
        } else {
          _install(cachedApk);
        }
      });
    } else {
      _install(widget.apk!);
    }
  }

  @override
  Widget build(BuildContext context) => WillPopScope(
    child: Scaffold(
      appBar: AppBar(
        title: const Text('Installing'),
        bottom: _progressBar ? PreferredSize(
          preferredSize: Size.zero,
          child: LinearProgressIndicator(value: _progress, minHeight: 6),
        ) : null,
        leading: _allowBack ? BackButton(onPressed: () => Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const HomePage()))) : null,
      ),
      body: LogsWidget(logs: _logs),
    ),
    onWillPop: () async {
      if (_allowBack) Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const HomePage()));
      return false;
    },
  );
}
