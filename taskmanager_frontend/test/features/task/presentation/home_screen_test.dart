// test/features/task/presentation/home_screen_test.dart
import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/features/tasks/data/task_model.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_bloc.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_event.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_state.dart';
import 'package:task_manager_app/features/tasks/presentation/home_screen.dart';
import 'package:task_manager_app/features/tasks/presentation/task_card.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

class MockTaskBloc extends MockBloc<TaskEvent, TaskState> implements TaskBloc {}
class MockThemeBloc extends MockBloc<ThemeEvent, ThemeState> implements ThemeBloc {}
class MockAuthBloc extends MockBloc<AuthEvent, AuthState> implements AuthBloc {}

class FakeTaskEvent extends Fake implements TaskEvent {}
class FakeTaskState extends Fake implements TaskState {}
class FakeThemeEvent extends Fake implements ThemeEvent {}
class FakeAuthEvent extends Fake implements AuthEvent {}

void main() {
  setUpAll(() {
    registerFallbackValue(FakeTaskEvent());
    registerFallbackValue(FakeTaskState());
    registerFallbackValue(FakeThemeEvent());
    registerFallbackValue(const ThemeState(ThemeMode.light));
    registerFallbackValue(FakeAuthEvent());
  });

  late MockTaskBloc mockTaskBloc;
  late MockThemeBloc mockThemeBloc;
  late MockAuthBloc mockAuthBloc;

  setUp(() {
    mockTaskBloc = MockTaskBloc();
    mockThemeBloc = MockThemeBloc();
    mockAuthBloc = MockAuthBloc();

    when(() => mockTaskBloc.state).thenReturn(TaskState.initial());
    when(() => mockThemeBloc.state).thenReturn(const ThemeState(ThemeMode.light));
    when(() => mockAuthBloc.state).thenReturn(AuthState.initial());

    whenListen<TaskState>(mockTaskBloc, const Stream<TaskState>.empty(), initialState: TaskState.initial());
    whenListen<ThemeState>(mockThemeBloc, const Stream<ThemeState>.empty(), initialState: const ThemeState(ThemeMode.light));
    whenListen<AuthState>(mockAuthBloc, const Stream<AuthState>.empty(), initialState: AuthState.initial());
  });

  Widget createTestWidget() {
    return MultiBlocProvider(
      providers: [
        BlocProvider<TaskBloc>.value(value: mockTaskBloc),
        BlocProvider<ThemeBloc>.value(value: mockThemeBloc),
        BlocProvider<AuthBloc>.value(value: mockAuthBloc),
      ],
      child: const MaterialApp(home: HomeScreen()),
    );
  }

  testWidgets('HomeScreen triggers LoadTasks on init', (tester) async {
    whenListen(
      mockTaskBloc,
      Stream<TaskState>.fromIterable([TaskState.initial()]),
      initialState: TaskState.initial(),
    );

    await tester.pumpWidget(createTestWidget());
    await tester.pump();

    verify(() => mockTaskBloc.add(any(that: isA<LoadTasks>()))).called(1);
  });

  testWidgets('Shows loading indicator when isLoading = true', (tester) async {
    when(() => mockTaskBloc.state).thenReturn(const TaskState(tasks: [], isLoading: true));
    whenListen<TaskState>(mockTaskBloc, const Stream<TaskState>.empty(), initialState: const TaskState(tasks: [], isLoading: true));

    await tester.pumpWidget(createTestWidget());
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgets('Shows empty message when no tasks', (tester) async {
    when(() => mockTaskBloc.state).thenReturn(const TaskState(tasks: [], isLoading: false));
    await tester.pumpWidget(createTestWidget());
    await tester.pump();

    expect(find.textContaining('No tasks yet'), findsOneWidget);
  });

  testWidgets('Displays tasks grid when tasks are available', (tester) async {
    final tasks = [
      const TaskModel(id: 1, title: 'Test Task 1', description: 'Desc'),
      const TaskModel(id: 2, title: 'Test Task 2', description: 'Desc'),
    ];

    when(() => mockTaskBloc.state).thenReturn(TaskState(tasks: tasks, isLoading: false));
    await tester.pumpWidget(createTestWidget());
    await tester.pump();

    expect(find.byType(TaskCard), findsNWidgets(2));
  });

  testWidgets('Pressing FAB opens AddTaskDialog (AlertDialog shown)', (tester) async {
    when(() => mockTaskBloc.state).thenReturn(TaskState.initial());
    await tester.pumpWidget(createTestWidget());
    await tester.pump();

    await tester.tap(find.byIcon(Icons.add));
    await tester.pumpAndSettle();

    // Assert a dialog is present (dialog title also says "Add Task")
    expect(find.byType(AlertDialog), findsOneWidget);
  });
}
