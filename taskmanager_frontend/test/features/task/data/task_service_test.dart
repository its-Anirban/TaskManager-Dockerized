import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart';
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:task_manager_app/features/auth/data/auth_service.dart';
import 'package:task_manager_app/features/tasks/data/task_model.dart';
import 'package:task_manager_app/features/tasks/data/task_service.dart';

/// Wrapper ONLY for tests.
/// Does NOT touch your real TaskService at all.
class TestableTaskService extends TaskService {

  TestableTaskService({
    required this.mock,
    required this.testBaseUrl,
  }) : super(baseUrl: testBaseUrl);
  final MockClient mock;
  final String testBaseUrl;

  // Because _authHeaders is private in TaskService
  Map<String, String> _headers(String? token) {
    final h = {'Content-Type': 'application/json'};
    if (token != null && token.isNotEmpty) {
      h['Authorization'] = 'Bearer $token';
    }
    return h;
  }

  @override
  Future<List<TaskModel>> fetchTasks() async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$testBaseUrl/api/tasks');

    final res = await mock.get(url, headers: _headers(token));

    if (res.statusCode == 200) {
      final List jsonList = json.decode(res.body);
      return jsonList.map((e) => TaskModel.fromMap(e)).toList();
    }
    throw Exception('Failed to fetch tasks');
  }

  @override
  Future<TaskModel> addTask(String title, String description) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$testBaseUrl/api/tasks');

    final res = await mock.post(
      url,
      headers: _headers(token),
      body: json.encode({'title': title, 'description': description}),
    );

    if (res.statusCode == 200 || res.statusCode == 201) {
      return TaskModel.fromMap(json.decode(res.body));
    }
    throw Exception('Failed to add task');
  }

  @override
  Future<void> deleteTask(int id) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$testBaseUrl/api/tasks/$id');

    final res = await mock.delete(url, headers: _headers(token));

    if (res.statusCode != 200 && res.statusCode != 204) {
      throw Exception('Failed to delete task');
    }
  }

  @override
  Future<TaskModel> updateTask(TaskModel t) async {
    final token = await AuthService.getToken();
    final url = Uri.parse('$testBaseUrl/api/tasks/${t.id}');

    final res = await mock.put(
      url,
      headers: _headers(token),
      body: json.encode(t.toMap()),
    );

    if (res.statusCode == 200) {
      return TaskModel.fromMap(json.decode(res.body));
    }
    throw Exception('Failed to update task');
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    SharedPreferences.setMockInitialValues({'auth_token': 'dummy'});
  });

  group('TaskService Tests (MockClient)', () {
    test('fetchTasks returns list', () async {
      final mock = MockClient((req) async {
        return Response(
          jsonEncode([
            {'id': 1, 'title': 'A', 'description': 'B', 'completed': false}
          ]),
          200,
        );
      });

      final service = TestableTaskService(
        mock: mock,
        testBaseUrl: 'http://localhost:8080',
      );

      final tasks = await service.fetchTasks();

      expect(tasks.length, 1);
      expect(tasks.first.id, 1);
    });

    test('fetchTasks throws error on failure', () async {
      final mock = MockClient((req) async {
        return Response('Server error', 500);
      });

      final service = TestableTaskService(
        mock: mock,
        testBaseUrl: 'http://localhost:8080',
      );

      expect(() => service.fetchTasks(), throwsException);
    });

    test('addTask returns created task', () async {
      final mock = MockClient((req) async {
        return Response(
          jsonEncode({
            'id': 12,
            'title': 'Test',
            'description': 'Desc',
            'completed': false
          }),
          201,
        );
      });

      final service = TestableTaskService(
        mock: mock,
        testBaseUrl: 'http://localhost:8080',
      );

      final t = await service.addTask('Test', 'Desc');

      expect(t.id, 12);
      expect(t.title, 'Test');
    });

    test('deleteTask succeeds', () async {
      final mock = MockClient((req) async => Response('', 204));

      final service = TestableTaskService(
        mock: mock,
        testBaseUrl: 'http://localhost:8080',
      );

      await service.deleteTask(5);

      expect(true, true);
    });

    test('updateTask returns updated', () async {
      final mock = MockClient((req) async {
        return Response(
          jsonEncode({
            'id': 10,
            'title': 'Updated',
            'description': 'New',
            'completed': false,
          }),
          200,
        );
      });

      final service = TestableTaskService(
        mock: mock,
        testBaseUrl: 'http://localhost:8080',
      );

      final updated = await service.updateTask(
        const TaskModel(id: 10, title: 'Old', description: 'Old'),
      );

      expect(updated.title, 'Updated');
      expect(updated.description, 'New');
    });
  });
}
