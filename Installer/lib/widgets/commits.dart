import 'package:flutter/material.dart';

import '../github.dart';
import '../utils/main.dart';

class CommitsWidget extends StatefulWidget {
  final void Function(String?) selectCommit;

  const CommitsWidget({ Key? key, required this.selectCommit }) : super(key: key);

  State<CommitsWidget> createState() => _CommitsWidgetState();
}

class _CommitsWidgetState extends State<CommitsWidget> {
  late Map<String, Commit> _commits;
  bool _initialized = false;

  void _getCommits() async {
    final buildCommits = await githubAPI!.getCommits(params: { 'sha': 'builds', 'path': 'Aliucord.dex' });
    final commits = await githubAPI!.getCommits(params: { 'per_page': '50' });
    _commits = Map.fromIterable(buildCommits.where((bc) => bc.commit.message.startsWith('Build ')), key: (bc) => bc.sha, value: (bc) {
      final sha = bc.commit.message.substring(6);
      return commits.firstWhere((c) => sha == c.sha);
    });
    setState(() => _initialized = true);
    widget.selectCommit(_commits.length > 0 ? _commits.entries.toList()[0].key : null);
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

  Widget build(BuildContext context) => Card(
    child: !_initialized || _commits.length == 0 ? Padding(padding: EdgeInsets.symmetric(vertical: 30), child: Center(
      child: _initialized ? Text('You\'re not logged in using an GitHub account.') : CircularProgressIndicator(),
    )) : Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(padding: EdgeInsets.all(15), child: Text('Commits', style: Theme.of(context).textTheme.subtitle1)),
        Flexible(child: Scrollbar(child: ListView.separated(
          itemBuilder: _buildCommit,
          separatorBuilder: (context, i) => Divider(),
          itemCount: _commits.length,
        ))),
      ],
    ),
  );

  Widget _buildCommit(BuildContext context, int i) {
    var commit = _commits.values.toList()[i];
    var linkStyle = TextStyle(color: Theme.of(context).accentColor);
    return Padding(padding: i != 0 ? EdgeInsets.all(12) : EdgeInsets.only(left: 12, right: 12, bottom: 12), child: Row(children: [
      Expanded(flex: 1, child: Row(children: [
        InkWell(
          child: Text(commit.sha.substring(0, 7), style: linkStyle),
          onTap: () => openUrl(commit.htmlUrl),
        ),
        SizedBox.shrink(),
      ])),
      Expanded(flex: 4, child: Text('${commit.commit.message.split('\n')[0]} - ${commit.author}', style: TextStyle(fontSize: 16))),
    ]));
  }
}
