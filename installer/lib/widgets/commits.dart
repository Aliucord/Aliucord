/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

import 'package:collection/collection.dart';
import 'package:flutter/material.dart';

import '../github.dart';
import '../utils/main.dart';

class _CommitData {
  final Commit commit;
  String? buildSha;

  _CommitData(this.commit, this.buildSha);
}

class CommitsWidget extends StatefulWidget {
  final void Function(String?) selectCommit;

  const CommitsWidget({ Key? key, required this.selectCommit }) : super(key: key);

  @override
  State<CommitsWidget> createState() => _CommitsWidgetState();
}

class _CommitsWidgetState extends State<CommitsWidget> {
  late List<_CommitData> _commits;
  bool _initialized = false;

  void _getCommits() async {
    final buildCommits = await githubAPI!.getCommits(params: { 'sha': 'builds', 'path': 'Injector.dex', 'per_page': '50' });
    final commits = await githubAPI!.getCommits(params: { 'per_page': '50' });
    _commits = commits.map((c) => _CommitData(c, buildCommits.firstWhereOrNull((bc) => bc.commit.message.substring(6) == c.sha)?.sha)).toList();
    setState(() => _initialized = true);

    widget.selectCommit(buildCommits.first.sha);
  }

  @override
  void initState() {
    super.initState();
    githubAPI!.addListener(_getCommits);
    _getCommits();
  }

  @override
  void dispose() {
    githubAPI!.removeListener(_getCommits);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Card(
    child: !_initialized || _commits.isEmpty ? const Padding(padding: EdgeInsets.symmetric(vertical: 30), child: Center(
      child: CircularProgressIndicator(),
    )) : Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(padding: const EdgeInsets.all(15), child: Text('Commits', style: Theme.of(context).textTheme.titleMedium)),
        Flexible(child: Scrollbar(child: ListView.separated(
          itemBuilder: _buildCommit,
          separatorBuilder: (context, i) => const Divider(),
          itemCount: _commits.length,
        ))),
      ],
    ),
  );

  Widget _buildCommit(BuildContext context, int i) {
    final commit = _commits[i].commit;
    final hasBuild = _commits[i].buildSha != null;
    final linkStyle = TextStyle(color: Theme.of(context).colorScheme.secondary);
    return Padding(padding: i != 0 ? const EdgeInsets.all(12) : const EdgeInsets.only(left: 12, right: 12, bottom: 12), child: Row(children: [
      Expanded(flex: 1, child: Row(children: [
        InkWell(
          child: Text(commit.sha.substring(0, 7), style: hasBuild ? linkStyle : const TextStyle(color: Colors.grey)),
          onTap: () => openUrl(commit.htmlUrl),
        ),
        const SizedBox.shrink(),
      ])),
      Expanded(flex: 4, child: Text('${commit.commit.message.split('\n')[0]} - ${commit.author}', style: const TextStyle(fontSize: 16))),
    ]));
  }
}
