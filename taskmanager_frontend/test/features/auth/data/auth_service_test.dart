// test/features/auth/data/auth_service_test.dart
import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/auth/data/auth_response.dart';
import 'package:task_manager_app/features/auth/data/auth_service.dart';
import 'package:task_manager_app/src/core/utils/constants.dart';

/// Mock http.Client
class MockHttpClient extends Mock implements http.Client {}

void main() {
  late MockHttpClient mockClient;
  late AuthService service;

  setUp(() {
    SharedPreferences.setMockInitialValues({});
    mockClient = MockHttpClient();

    service = AuthService(
      baseUrl: 'http://test.com',
      client: mockClient, // <-- injected client
    );

    // Needed for any() on headers/body
    registerFallbackValue(Uri());
    registerFallbackValue(<String, String>{});
  });

  group('AuthService (no prod code changes)', () {
    test('login success saves token & returns LoginResponse', () async {
      final responseJson = jsonEncode({'token': 'xyz123'});

      when(() => mockClient.post(
            Uri.parse('http://test.com/api/auth/login'),
            headers: any(named: 'headers'),
            body: any(named: 'body'),
          )).thenAnswer(
        (_) async => http.Response(responseJson, 200),
      );

      final result = await service.login('user', 'pass');

      expect(result, isA<LoginResponse>());
      expect(result.token, 'xyz123');

      final prefs = await SharedPreferences.getInstance();
      expect(prefs.getString(Constants.tokenKey), 'xyz123');
    });

    test('register success (201) returns normally', () async {
      when(() => mockClient.post(
            Uri.parse('http://test.com/api/auth/register'),
            headers: any(named: 'headers'),
            body: any(named: 'body'),
          )).thenAnswer(
        (_) async => http.Response('', 201),
      );

      await service.register('a', 'b');

      // If no exception → success
      expect(true, true);
    });

    test('login failure throws exception', () async {
      when(() => mockClient.post(
            Uri.parse('http://test.com/api/auth/login'),
            headers: any(named: 'headers'),
            body: any(named: 'body'),
          )).thenAnswer(
        (_) async => http.Response('bad', 400),
      );

      expect(
        () => service.login('u', 'p'),
        throwsA(isA<Exception>()),
      );
    });
  });
}
