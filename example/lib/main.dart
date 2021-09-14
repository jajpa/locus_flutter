import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:locus_flutter/locus_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await LocusFlutter.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Center(
            child: Column(
              children: [
                Text('Running on: $_platformVersion\n'),
                SizedBox(height: 16),
                StreamBuilder<String>(
                    stream: LocusFlutter.sdkEventsStream,
                    builder: (context, snapshot) {
                      if (!snapshot.hasData) return CircularProgressIndicator();
                      return Text('${snapshot.data}');
                    }),
                ElevatedButton(
                  onPressed: () async {
                    var res = await LocusFlutter.login({
                      'clientId': 'arrivedelivery-demo',
                      'userId': 'test',
                      'password': '342315',
                    });
                    print(res);
                  },
                  child: Text('Login'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    var res = await LocusFlutter.logout();
                    print(res);
                  },
                  child: Text('Logout'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    var res = await LocusFlutter.startTracking();
                    print(res);
                  },
                  child: Text('Start tracking'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    var res = await LocusFlutter.getSdkState();
                    print(res);
                  },
                  child: Text('SDK State'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    var res = await LocusFlutter.stopTracking();
                    print(res);
                  },
                  child: Text('Stop tracking'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
