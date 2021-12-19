/*
 * Copyright (c) 2021 Juby210 & Vendicated
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

class Release {
  final String tag;
  final Iterable<Asset> assets;

  const Release(this.tag, this.assets);

  Release.fromJson(Map<String, dynamic> json)
    : tag = json['tag_name'],
      assets = (json['assets'] as Iterable<dynamic>)
        .map((e) => Asset(e['name'], e['browser_download_url']));
}

class Asset {
  final String name;
  final String downloadUrl;

  const Asset(this.name, this.downloadUrl);
}

class GithubAPI with ChangeNotifier {
  static const _org = 'Aliucord';
  static const _repo = 'Aliucord';

  static const _apiHost = 'api.github.com';
  static const _commitsEndpoint = '/repos/$_org/$_repo/commits';
  static const _latestReleaseEndpoint = '/repos/$_org/$_repo/releases/latest';

  GithubAPI() {
    checkForUpdates();
  }

  void checkForUpdates() async {
    final release = await getLatestRelease();
    if (release == null) return;
    final currentCommit = await getGitRev();
    if (release.tag == currentCommit) return;
    final commit = await getCommit(release.tag);
    showDialog(context: navigatorKey.currentContext!, barrierDismissible: false, builder: (context) => UpdateDialog(
      commit: release.tag,
      currentCommit: currentCommit,
      message: commit?.commit.message ?? 'Couldn\'t fetch commit message',
      downloadUrl: release.assets.firstWhere((e) => e.name == 'Installer-release.apk').downloadUrl,
    ));
  }

  final Map<Uri, Iterable<Commit>> _commitsCache = {};
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
    } on DioError {
      // nop
    }
    return [];
  }

  Future<Commit?> getCommit(String commit) async {
    try {
      final res = await dio.getUri(Uri.https(_apiHost, '$_commitsEndpoint/$commit'));
      if (res.data is Map<String, dynamic>) return Commit.fromJson(res.data);
    } on DioError {
      // nop
    }
    return null;
  }

  Future<Release?> getLatestRelease() async {
    try {
      final res = await dio.getUri(Uri.https(_apiHost, _latestReleaseEndpoint));
      if (res.data is Map<String, dynamic>) return Release.fromJson(res.data);
    } on DioError {
      // nop
    }
    return null;
  }

  String getDownloadUrl(String ref, String file) => 'https://raw.githubusercontent.com/$_org/$_repo/$ref/$file';
}
