import 'dart:async';

import 'package:flutter/services.dart';

class LocusFlutter {
  static const _channel = const MethodChannel('locus_flutter');
  static const _eventChannel = const EventChannel('locus_flutter_stream');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> login(Map<String, dynamic> body) async {
    return await _channel.invokeMethod('login', body);
  }

  static Future<String?> logout() async {
    final String? version = await _channel.invokeMethod('logout');
    return version;
  }

  static Future<String?> startTracking() async {
    final String? version = await _channel.invokeMethod('startTracking');
    return version;
  }

  static Future<String?> stopTracking() async {
    final String? version = await _channel.invokeMethod('stopTracking');
    return version;
  }

  static Future<String?> getSdkState() async {
    final String? version = await _channel.invokeMethod('getSdkState');
    return version;
  }

  static Stream<String> get sdkEventsStream {
    return _eventChannel.receiveBroadcastStream().map((event) => event);
  }
}
