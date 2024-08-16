/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2024 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'dart:async';

import 'package:flutter/material.dart';

import '../utils/main.dart';

class ManagerDialog extends StatefulWidget {
  final void Function()? onDismiss;

  const ManagerDialog({this.onDismiss, super.key});

  @override
  State<StatefulWidget> createState() => _ManagerDialogState();
}

class _ManagerDialogState extends State<ManagerDialog> {
  void Function()? _dismiss;
  Timer? _timer;
  int _seconds = 5;

  @override
  void dispose() {
    if (_timer?.isActive == true) _timer!.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _timer ??= Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_seconds == 1) {
        timer.cancel();
        _dismiss = () {
          if (widget.onDismiss == null) {
            Navigator.of(context, rootNavigator: true).pop();
          } else {
            widget.onDismiss!.call();
          }
        };
      }
      setState(() => _seconds--);
    });
    return PopScope(
      canPop: _seconds == 0,
      child: AlertDialog(
        title: const Text('Installer is no longer supported'),
        content: const Text(
            'Please install new Aliucord Manager app for managing (installing, updating) Aliucord.\n\nAliucord Manager has improved user experience and support for new Android versions.\n\nAlso in the future some updates will require Aliucord apk update using Manager.\n\nUsing Installer may not install Aliucord correctly and may result with broken installation.'),
        actions: [
          TextButton(
            onPressed: _dismiss,
            child: Row(mainAxisSize: MainAxisSize.min, children: [
              const Icon(Icons.cancel_outlined),
              Text(
                  ' Dismiss (${_seconds == 0 ? 'not recommended' : _seconds})'),
            ]),
          ),
          TextButton(
            onPressed: () =>
                openUrl("https://github.com/Aliucord/Manager/releases"),
            child: const Row(mainAxisSize: MainAxisSize.min, children: [
              Icon(Icons.file_download),
              Text(' Install Manager'),
            ]),
          ),
        ],
      ),
    );
  }
}
