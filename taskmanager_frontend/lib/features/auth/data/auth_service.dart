import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/auth/data/auth_response.dart';
import 'package:task_manager_app/src/core/utils/constants.dart';

class AuthService {
  AuthService({
    String? baseUrl,
    http.Client? client,
  })  : _baseUrl = baseUrl ?? Constants.baseUrl,
        _client = client ?? http.Client();

  final String _baseUrl;
  final http.Client _client;

  Future<LoginResponse> login(String username, String password) async {
    final url = Uri.parse('$_baseUrl/api/auth/login');

    final res = await _client.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'username': username, 'password': password}),
    );

    if (res.statusCode == 200) {
      final data = json.decode(res.body);
      final lr = LoginResponse.fromMap(data);

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(Constants.tokenKey, lr.token);

      return lr;
    } else {
      final msg = res.body.isNotEmpty ? res.body : 'Login failed';
      throw Exception('Login failed: ${res.statusCode} - $msg');
    }
  }

  Future<void> register(String username, String password) async {
    final url = Uri.parse('$_baseUrl/api/auth/register');

    final res = await _client.post(
      url,
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'username': username, 'password': password}),
    );

    if (res.statusCode != 200 && res.statusCode != 201) {
      throw Exception('Registration failed: ${res.statusCode} - ${res.body}');
    }
  }

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(Constants.tokenKey);
  }

  static Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(Constants.tokenKey);
  }
}
