/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../utils/main.dart';

class UpdateDialog extends StatefulWidget {
  final String commit;
  final String currentCommit;
  final String message;

  const UpdateDialog({
    Key? key,
    required this.commit,
    required this.currentCommit,
    required this.message
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
    final outPath = storageRoot.path + '/Aliucord/Installer.apk';
    try {
      final url = githubAPI!.getDownloadUrl('builds', 'Installer-release.apk');
      await dio.download(url, outPath, onReceiveProgress: (count, total) => setState(() => _progress = count / total));
      installApk(outPath);
    } on DioError catch (e) {
      toast('Update failed: ${e.error}');
    }
    _dismiss();
  }

  @override
  Widget build(BuildContext context) => _updating ? AlertDialog(
    title: Text('Updating.. ' + (_progress == null ? '' : (_progress! * 100).round().toString() + '%')),
    content: LinearProgressIndicator(value: _progress),
  ) : AlertDialog(
    title: const Text('Update available'),
    content: Text('A new version is available: ${widget.commit} ${widget.message}\ncurrent version: ${widget.currentCommit}'),
    actions: [
      TextButton(
        child: Row(children: const [
          Icon(Icons.cancel_outlined),
          Text(' Cancel'),
        ], mainAxisSize: MainAxisSize.min),
        onPressed: _dismiss,
      ),
      TextButton(
        child: Row(children: const [
          Icon(Icons.file_download),
          Text(' Update'),
        ], mainAxisSize: MainAxisSize.min),
        onPressed: _update,
      ),
    ],
  );
}
