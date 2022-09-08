/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:flutter/material.dart';

class LogsWidget extends StatelessWidget {
  final ScrollController _controller = ScrollController();
  final String logs;

  LogsWidget({ Key? key, required this.logs }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    WidgetsBinding.instance.addPostFrameCallback((_) => _controller.jumpTo(_controller.position.maxScrollExtent));
    return SingleChildScrollView(
      controller: _controller,
      child: SelectableText(logs, style: const TextStyle(fontFamily: 'monospace')),
    );
  }
}
