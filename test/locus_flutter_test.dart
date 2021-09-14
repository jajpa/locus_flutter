import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:locus_flutter/locus_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('locus_flutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await LocusFlutter.platformVersion, '42');
  });
}
