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
  static const String _clientId = '9260a23baccb4ebf2d94';
  static const String _clientSecret = '0ab74f8ed58600ad48acbb9843ab05615ec4940d';
  static const String _org = 'Aliucord';
  static const String _repo = 'Aliucord';

  static const String _authUrl = 'https://github.com/login/oauth/authorize?client_id=$_clientId&redirect_uri=aliucord-installer%3A%2F%2Fauth&scope=repo';
  static const String _oauthAccessToken = 'https://github.com/login/oauth/access_token';

  static const String _apiHost = 'api.github.com';
  static const String _commitsEndpoint = '/repos/$_org/$_repo/commits';
  static const String _contentsEndpoint = '/repos/$_org/$_repo/contents';

  String authToken = prefs.getString('github_token') ?? '';

  GithubAPI() {
    if (isAuthenticated()) checkForUpdates();
  }

  void startAuthFlow() {
    if (isAuthenticated()) {
      prefs.remove('github_token');
      authToken = '';
      notifyListeners();
    } else channel.invokeMethod('openChromeCustomTab', _authUrl);
  }

  void authCallback(String code) async {
    try {
      final res = await dio.post(
        _oauthAccessToken,
        data: { 'code': code, 'client_id': _clientId, 'client_secret': _clientSecret },
        options: Options(contentType: Headers.formUrlEncodedContentType, headers: { Headers.acceptHeader: Headers.jsonContentType }),
      );
      String? token = res.data['access_token'];
      if (token != null) {
        prefs.setString('github_token', token);
        authToken = token;
        checkForUpdates();
        notifyListeners();
      } else {
        print(res.data);
        toast('Failed to get access token');
      }
    } on DioError catch (e) {
      if (e.response != null) print(e.response!.data);
      toast('Failed to get access token');
    }
  }

  bool isAuthenticated() => authToken != '';

  void checkForUpdates() async {
    final commits = await getCommits(params: { 'sha': 'builds', 'path': 'Installer-release.apk' });
    if (commits.length == 0) return;
    final msg = commits.toList()[0].commit.message;
    if (msg.length < 23) return;
    final commit = msg.substring(16, 23);
    final currentCommit = await getGitRev();
    if (commit == currentCommit) return;
    final res = await dio.getUri(
      Uri.https(_apiHost, '$_commitsEndpoint/$commit'),
      options: Options(headers: { 'Authorization': 'token $authToken' }),
    );
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
    if (!isAuthenticated()) return [];
    final commitsUri = Uri.https(_apiHost, _commitsEndpoint, params);
    if (_commitsCache.containsKey(commitsUri)) return _commitsCache[commitsUri]!;
    try {
      final res = await dio.getUri(
        commitsUri,
        options: Options(headers: { 'Authorization': 'token $authToken' }),
      );
      if (res.data is List) {
        final commits = List<Map<String, dynamic>>.from(res.data).map((e) => Commit.fromJson(e));
        _commitsCache[commitsUri] = commits;
        return commits;
      }
    } on DioError {}
    return [];
  }

  Map<String, Map<String, String>> _contents = {};
  Future<String?> getDownloadUrl(String ref, String file) async {
    if (!isAuthenticated()) return null;
    if (_contents.containsKey(ref)) return _contents[ref]?[file];
    final res = await dio.getUri(
      Uri.https(_apiHost, _contentsEndpoint, { 'ref': ref }),
      options: Options(headers: { 'Authorization': 'token $authToken' }),
    );
    if (res.data is List) {
      _contents[ref] = Map.fromIterable(res.data, key: (e) => e['name'], value: (e) => e['download_url']);
      return _contents[ref]?[file];
    }
    return null;
  }
}
