/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import 'utils/main.dart';
import 'widgets/update_dialog.dart';

class Commit {
  final String htmlUrl;
  final String sha;
  final CommitCommit commit;
  final String author;

  Commit({ required this.htmlUrl, required this.sha, required this.commit, required this.author });

  Commit.fromJson(Map<String, dynamic> json)
    : htmlUrl = json['html_url'],
      sha = json['sha'],
      commit = CommitCommit(json['commit']['message']),
      author = json['author']['login'];
}

class CommitCommit {
  final String message;

  CommitCommit(this.message);
}

class GithubAPI with ChangeNotifier {
  static const String _org = 'Aliucord';
  static const String _repo = 'Aliucord';

  static const String _apiHost = 'api.github.com';
  static const String _commitsEndpoint = '/repos/$_org/$_repo/commits';

  GithubAPI() {
    checkForUpdates();
  }

  void checkForUpdates() async {
    final commits = await getCommits(params: { 'sha': 'builds', 'path': 'Installer-release.apk' });
    if (commits.length == 0) return;
    final msg = commits.toList()[0].commit.message;
    if (msg.length < 23) return;
    final commit = msg.substring(16, 23);
    final currentCommit = await getGitRev();
    if (commit == currentCommit) return;
    final res = await dio.getUri(Uri.https(_apiHost, '$_commitsEndpoint/$commit'));
    final String? message = res.data?['commit']?['message'];
    if (message == null) return;
    showDialog(context: navigatorKey.currentContext!, barrierDismissible: false, builder: (context) => UpdateDialog(
      commit: commit,
      currentCommit: currentCommit,
      message: message,
    ));
  }

  Map<Uri, Iterable<Commit>> _commitsCache = {};
  Future<Iterable<Commit>> getCommits({ Map<String, dynamic>? params }) async {
    final commitsUri = Uri.https(_apiHost, _commitsEndpoint, params);
    if (_commitsCache.containsKey(commitsUri)) return _commitsCache[commitsUri]!;
    try {
      final res = await dio.getUri(commitsUri);
      if (res.data is List) {
        final commits = List<Map<String, dynamic>>.from(res.data).map((e) => Commit.fromJson(e));
        _commitsCache[commitsUri] = commits;
        return commits;
      }
    } on DioError {}
    return [];
  }

  String getDownloadUrl(String ref, String file) {
    return 'https://raw.githubusercontent.com/$_org/$_repo/$ref/$file';
  }
}
