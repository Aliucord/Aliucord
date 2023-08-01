/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../utils/main.dart';

class UpdateDialog extends StatefulWidget {
  final String newVersion;
  final String currentVersion;
  final String message;
  final String downloadUrl;

  const UpdateDialog({
    Key? key,
    required this.newVersion,
    required this.currentVersion,
    required this.message,
    required this.downloadUrl,
  }) : super(key: key);

  @override
  State<UpdateDialog> createState() => _UpdateDialogState();
}

class _UpdateDialogState extends State<UpdateDialog> {
  bool _updating = false;
  double? _progress;

  void _dismiss() => Navigator.of(context, rootNavigator: true).pop();

  void _update() async {
    setState(() => _updating = true);
    final outPath = '${storageRoot.path}/Aliucord/Installer.apk';
    try {
      await dio.download(
        widget.downloadUrl,
        outPath,
        onReceiveProgress: (count, total) => setState(() => _progress = count / total),
      );
      installApk(outPath);
    } on DioException catch (e) {
      toast('Update failed: $e');
    }
    _dismiss();
  }

  @override
  Widget build(BuildContext context) => _updating ? AlertDialog(
    title: Text('Updating.. ${_progress == null ? '' : '${(_progress! * 100).round()}%'}'),
    content: LinearProgressIndicator(value: _progress),
  ) : AlertDialog(
    title: const Text('Update available'),
    content: Text('A new version is available: ${widget.newVersion} ${widget.message}\ncurrent version: ${widget.currentVersion}'),
    actions: [
      TextButton(
        onPressed: _dismiss,
        child: const Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.cancel_outlined),
          Text(' Cancel'),
        ]),
      ),
      TextButton(
        onPressed: _update,
        child: const Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.file_download),
          Text(' Update'),
        ]),
      ),
    ],
  );
}
