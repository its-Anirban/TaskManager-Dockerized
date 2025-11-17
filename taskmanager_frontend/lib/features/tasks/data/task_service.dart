import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:task_manager_app/features/auth/data/auth_service.dart';
import 'package:task_manager_app/features/tasks/data/task_model.dart';
import 'package:task_manager_app/src/core/utils/constants.dart';

class TaskService {
  /// Uses provided baseUrl or falls back to Constants.baseUrl
  TaskService({String? baseUrl}) : _baseUrl = baseUrl ?? Constants.baseUrl;

  final String _baseUrl;

  Future<List<TaskModel>> fetchTasks() async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$_baseUrl/api/tasks');

    final res = await http.get(url, headers: _authHeaders(token));

    if (res.statusCode == 200) {
      final List list = json.decode(res.body) as List;
      return list.map((e) => TaskModel.fromMap(e)).toList();
    } else {
      throw Exception('Failed to fetch tasks: ${res.statusCode} ${res.body}');
    }
  }

  Future<TaskModel> addTask(String title, String description) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$_baseUrl/api/tasks');

    final res = await http.post(
      url,
      headers: _authHeaders(token),
      body: json.encode({'title': title, 'description': description}),
    );

    if (res.statusCode == 200 || res.statusCode == 201) {
      return TaskModel.fromMap(json.decode(res.body));
    } else {
      throw Exception('Failed to add task: ${res.statusCode} ${res.body}');
    }
  }

  Future<void> deleteTask(int id) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$_baseUrl/api/tasks/$id');

    final res = await http.delete(url, headers: _authHeaders(token));

    if (res.statusCode != 200 && res.statusCode != 204) {
      throw Exception('Failed to delete task: ${res.statusCode} ${res.body}');
    }
  }

  Future<TaskModel> updateTask(TaskModel t) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$_baseUrl/api/tasks/${t.id}');

    final res = await http.put(
      url,
      headers: _authHeaders(token),
      body: json.encode(t.toMap()),
    );

    if (res.statusCode == 200) {
      return TaskModel.fromMap(json.decode(res.body));
    } else {
      throw Exception('Failed to update task: ${res.statusCode} ${res.body}');
    }
  }

  Map<String, String> _authHeaders(String? token) {
    final headers = {'Content-Type': 'application/json'};
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }
}
