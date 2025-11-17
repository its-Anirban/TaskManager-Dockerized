import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
// Auth imports
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/features/auth/presentation/login_screen.dart';
import 'package:task_manager_app/features/auth/presentation/register_screen.dart';
// Task imports
import 'package:task_manager_app/features/tasks/presentation/bloc/task_bloc.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_event.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_state.dart';
import 'package:task_manager_app/features/tasks/presentation/home_screen.dart';
// Theme imports
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

/// ------------------------------------------------------------
/// MOCKS
/// ------------------------------------------------------------
class MockAuthBloc extends MockBloc<AuthEvent, AuthState>
    implements AuthBloc {}

class MockThemeBloc extends MockBloc<ThemeEvent, ThemeState>
    implements ThemeBloc {}

class MockTaskBloc extends MockBloc<TaskEvent, TaskState>
    implements TaskBloc {}

class FakeAuthEvent extends Fake implements AuthEvent {}
class FakeAuthState extends Fake implements AuthState {}
class FakeThemeEvent extends Fake implements ThemeEvent {}
class FakeThemeState extends Fake implements ThemeState {}
class FakeTaskEvent extends Fake implements TaskEvent {}
class FakeTaskState extends Fake implements TaskState {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUpAll(() {
    registerFallbackValue(FakeAuthEvent());
    registerFallbackValue(FakeAuthState());
    registerFallbackValue(FakeThemeEvent());
    registerFallbackValue(FakeThemeState());
    registerFallbackValue(FakeTaskEvent());
    registerFallbackValue(FakeTaskState());
  });

  late MockAuthBloc authBloc;
  late MockThemeBloc themeBloc;
  late MockTaskBloc taskBloc;

  setUp(() {
    authBloc = MockAuthBloc();
    themeBloc = MockThemeBloc();
    taskBloc = MockTaskBloc();

    when(() => authBloc.state)
        .thenReturn(const AuthState(isLoading: false, isAuthenticated: false));

    when(() => themeBloc.state)
        .thenReturn(const ThemeState(ThemeMode.light));

    // Use TaskState.initial() (factory) — TaskInitial does not exist
    when(() => taskBloc.state).thenReturn(TaskState.initial());

    whenListen(
      authBloc,
      const Stream<AuthState>.empty(),
      initialState:
          const AuthState(isLoading: false, isAuthenticated: false),
    );

    whenListen(
      themeBloc,
      const Stream<ThemeState>.empty(),
      initialState: const ThemeState(ThemeMode.light),
    );

    whenListen(
      taskBloc,
      const Stream<TaskState>.empty(),
      initialState: TaskState.initial(),
    );
  });

  /// ------------------------------------------------------------
  /// Helper to pump LoginScreen with ALL required blocs
  /// ------------------------------------------------------------
  Future<void> pumpWithBlocs(WidgetTester tester) async {
    await tester.pumpWidget(
      MultiBlocProvider(
        providers: [
          BlocProvider<AuthBloc>.value(value: authBloc),
          BlocProvider<ThemeBloc>.value(value: themeBloc),
          BlocProvider<TaskBloc>.value(value: taskBloc),
        ],
        child: const MaterialApp(home: LoginScreen()),
      ),
    );
    await tester.pump();
  }

  /// ------------------------------------------------------------
  /// TESTS
  /// ------------------------------------------------------------
  testWidgets('renders UI elements', (tester) async {
    await pumpWithBlocs(tester);

    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Login'), findsOneWidget);
    expect(find.widgetWithText(TextButton, 'Create an Account'), findsOneWidget);
  });

  testWidgets('password visibility toggles', (tester) async {
    await pumpWithBlocs(tester);

    await tester.tap(find.byIcon(Icons.visibility_off));
    await tester.pump();

    expect(find.byIcon(Icons.visibility), findsOneWidget);
  });

  testWidgets('invalid form prevents login dispatch', (tester) async {
    await pumpWithBlocs(tester);

    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    verifyNever(() => authBloc.add(any()));
  });

  testWidgets('valid login dispatches LoginRequested', (tester) async {
    await pumpWithBlocs(tester);

    await tester.enterText(find.byType(TextFormField).at(0), 'test');
    await tester.enterText(find.byType(TextFormField).at(1), '1234');

    await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
    await tester.pump();

    verify(() => authBloc.add(const LoginRequested('test', '1234'))).called(1);
  });

  testWidgets('shows loading indicator', (tester) async {
    when(() => authBloc.state)
        .thenReturn(const AuthState(isLoading: true, isAuthenticated: false));

    await pumpWithBlocs(tester);

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgets('navigates to HomeScreen when authenticated', (tester) async {
    whenListen(
      authBloc,
      Stream.value(const AuthState(isLoading: false, isAuthenticated: true)),
      initialState: const AuthState(isLoading: false, isAuthenticated: false),
    );

    await pumpWithBlocs(tester);
    await tester.pumpAndSettle();

    expect(find.byType(HomeScreen), findsOneWidget);
  });

  testWidgets('navigates to RegisterScreen', (tester) async {
    await pumpWithBlocs(tester);

    await tester.tap(find.widgetWithText(TextButton, 'Create an Account'));
    await tester.pumpAndSettle();

    expect(find.byType(RegisterScreen), findsOneWidget);
  });

  testWidgets('theme toggle dispatches ToggleTheme', (tester) async {
    await pumpWithBlocs(tester);

    await tester.tap(find.byIcon(Icons.nightlight_round));
    await tester.pump();

    verify(() => themeBloc.add(ToggleTheme())).called(1);
  });
}
