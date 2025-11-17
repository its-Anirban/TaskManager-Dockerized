import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/tasks/data/task_model.dart';
import 'package:task_manager_app/features/tasks/data/task_service.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_bloc.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_event.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_state.dart';

class MockTaskService extends Mock implements TaskService {}

class FakeTaskModel extends Fake implements TaskModel {}

void main() {
  late MockTaskService mockService;

  setUpAll(() {
    registerFallbackValue(FakeTaskModel());
  });

  setUp(() {
    mockService = MockTaskService();

    // Reset SharedPreferences (used for AuthService.getToken())
    SharedPreferences.setMockInitialValues({
      'token': 'dummy_token',
    });
  });

  group('TaskBloc Tests', () {
    blocTest<TaskBloc, TaskState>(
      'LoadTasks → success',
      build: () {
        when(() => mockService.fetchTasks()).thenAnswer(
          (_) async => [
            const TaskModel(id: 1, title: 'A', description: 'desc'),
            const TaskModel(id: 2, title: 'B', description: 'desc'),
          ],
        );
        return TaskBloc(mockService);
      },
      act: (bloc) => bloc.add(LoadTasks()),
      expect: () => [
        const TaskState(tasks: [], isLoading: true, errorMessage: ''),
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'A', description: 'desc'),
            TaskModel(id: 2, title: 'B', description: 'desc'),
          ],
          isLoading: false,
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'LoadTasks → failure',
      build: () {
        when(() => mockService.fetchTasks())
            .thenThrow(Exception('Fail fetch'));

        return TaskBloc(mockService);
      },
      act: (bloc) => bloc.add(LoadTasks()),
      expect: () => [
        const TaskState(tasks: [], isLoading: true, errorMessage: ''),
        const TaskState(
          tasks: [],
          isLoading: false,
          errorMessage: 'Exception: Fail fetch',
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'AddTask → success',
      build: () {
        const createdTask =
            TaskModel(id: 99, title: 'New', description: 'desc');

        when(() => mockService.addTask('New', 'desc'))
            .thenAnswer((_) async => createdTask);

        return TaskBloc(mockService);
      },
      act: (bloc) => bloc.add(const AddTask('New', 'desc')),
      expect: () => [
        const TaskState(tasks: [], isLoading: true, errorMessage: ''),
        const TaskState(
          tasks: [
            TaskModel(id: 99, title: 'New', description: 'desc'),
          ],
          isLoading: false,
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'AddTask → failure',
      build: () {
        when(() => mockService.addTask('New', 'desc'))
            .thenThrow(Exception('Add failed'));

        return TaskBloc(mockService);
      },
      act: (bloc) => bloc.add(const AddTask('New', 'desc')),
      expect: () => [
        const TaskState(tasks: [], isLoading: true, errorMessage: ''),
        const TaskState(
          tasks: [],
          isLoading: false,
          errorMessage: 'Exception: Add failed',
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'DeleteTask → success',
      build: () {
        when(() => mockService.deleteTask(1))
            .thenAnswer((_) async => Future.value());

        return TaskBloc(mockService)
          ..emit(
            const TaskState(
              tasks: [
                TaskModel(id: 1, title: 'Old', description: 'desc'),
                TaskModel(id: 2, title: 'Keep', description: 'desc'),
              ],
              isLoading: false,
            ),
          );
      },
      act: (bloc) => bloc.add(const DeleteTask(1)),
      expect: () => [
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
            TaskModel(id: 2, title: 'Keep', description: 'desc'),
          ],
          isLoading: true,
          errorMessage: '',
        ),
        const TaskState(
          tasks: [
            TaskModel(id: 2, title: 'Keep', description: 'desc'),
          ],
          isLoading: false,
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'DeleteTask → failure',
      build: () {
        when(() => mockService.deleteTask(1))
            .thenThrow(Exception('Delete failed'));

        return TaskBloc(mockService)
          ..emit(
            const TaskState(
              tasks: [
                TaskModel(id: 1, title: 'Old', description: 'desc'),
              ],
              isLoading: false,
            ),
          );
      },
      act: (bloc) => bloc.add(const DeleteTask(1)),
      expect: () => [
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
          ],
          isLoading: true,
          errorMessage: '',
        ),
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
          ],
          isLoading: false,
          errorMessage: 'Exception: Delete failed',
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'UpdateTask → success',
      build: () {
        const original = TaskModel(
            id: 1, title: 'Old', description: 'desc');
        const updated = TaskModel(
            id: 1, title: 'Updated', description: 'new', completed: true);

        when(() => mockService.updateTask(updated))
            .thenAnswer((_) async => updated);

        return TaskBloc(mockService)
          ..emit(const TaskState(tasks: [original], isLoading: false));
      },
      act: (bloc) => bloc.add(const UpdateTask(TaskModel(
        id: 1,
        title: 'Updated',
        description: 'new',
        completed: true,
      ))),
      expect: () => [
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
          ],
          isLoading: true,
          errorMessage: '',
        ),
        const TaskState(
          tasks: [
            TaskModel(
              id: 1,
              title: 'Updated',
              description: 'new',
              completed: true,
            )
          ],
          isLoading: false,
        ),
      ],
    );

    blocTest<TaskBloc, TaskState>(
      'UpdateTask → failure',
      build: () {
        const original = TaskModel(
            id: 1, title: 'Old', description: 'desc');

        when(() => mockService.updateTask(any()))
            .thenThrow(Exception('Update failed'));

        return TaskBloc(mockService)
          ..emit(const TaskState(tasks: [original], isLoading: false));
      },
      act: (bloc) => bloc.add(const UpdateTask(TaskModel(
        id: 1,
        title: 'X',
        description: 'Y',
      ))),
      expect: () => [
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
          ],
          isLoading: true,
          errorMessage: '',
        ),
        const TaskState(
          tasks: [
            TaskModel(id: 1, title: 'Old', description: 'desc'),
          ],
          isLoading: false,
          errorMessage: 'Exception: Update failed',
        ),
      ],
    );
  });
}
