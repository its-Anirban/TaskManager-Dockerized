import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/features/auth/presentation/register_screen.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

/// ---------------------------------------------------------------------------
/// Mock classes
/// ---------------------------------------------------------------------------
class MockAuthBloc extends MockBloc<AuthEvent, AuthState> implements AuthBloc {}

class MockThemeBloc extends MockBloc<ThemeEvent, ThemeState>
    implements ThemeBloc {}

class FakeAuthEvent extends Fake implements AuthEvent {}

class FakeAuthState extends Fake implements AuthState {}

class FakeThemeEvent extends Fake implements ThemeEvent {}

class FakeThemeState extends Fake implements ThemeState {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUpAll(() {
    registerFallbackValue(FakeAuthEvent());
    registerFallbackValue(FakeAuthState());
    registerFallbackValue(FakeThemeEvent());
    registerFallbackValue(FakeThemeState());
  });

  late MockAuthBloc mockAuthBloc;
  late MockThemeBloc mockThemeBloc;

  setUp(() {
    mockAuthBloc = MockAuthBloc();
    mockThemeBloc = MockThemeBloc();

    when(() => mockAuthBloc.state)
        .thenReturn(const AuthState(isLoading: false, isAuthenticated: false));

    when(() => mockThemeBloc.state)
        .thenReturn(const ThemeState(ThemeMode.light));

    whenListen(
      mockAuthBloc,
      const Stream<AuthState>.empty(),
      initialState:
          const AuthState(isLoading: false, isAuthenticated: false),
    );

    whenListen(
      mockThemeBloc,
      const Stream<ThemeState>.empty(),
      initialState: const ThemeState(ThemeMode.light),
    );
  });

  /// Helper Pump
  Future<void> pumpWithBlocs(WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: MultiBlocProvider(
          providers: [
            BlocProvider<AuthBloc>.value(value: mockAuthBloc),
            BlocProvider<ThemeBloc>.value(value: mockThemeBloc),
          ],
          child: const RegisterScreen(),
        ),
      ),
    );

    await tester.pump();
  }

  /// ---------------------------------------------------------------------------
  /// TEST 1 — Render fields
  /// ---------------------------------------------------------------------------
  testWidgets('renders username, password and buttons', (tester) async {
    await pumpWithBlocs(tester);

    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Sign Up'), findsOneWidget);
    expect(find.widgetWithText(TextButton, 'Back to Login'), findsOneWidget);
  });

  /// ---------------------------------------------------------------------------
  /// TEST 2 — Password visibility toggle
  /// ---------------------------------------------------------------------------
  testWidgets('password visibility toggles', (tester) async {
    await pumpWithBlocs(tester);

    final visibilityOff = find.byIcon(Icons.visibility_off_outlined);
    expect(visibilityOff, findsOneWidget);

    await tester.tap(visibilityOff);
    await tester.pump();

    expect(find.byIcon(Icons.visibility_outlined), findsOneWidget);
  });

  /// ---------------------------------------------------------------------------
  /// TEST 3 — Prevent dispatch if invalid
  /// ---------------------------------------------------------------------------
  testWidgets('validation prevents RegisterRequested dispatch', (tester) async {
    await pumpWithBlocs(tester);

    await tester.tap(find.widgetWithText(ElevatedButton, 'Sign Up'));
    await tester.pump();

    verifyNever(() => mockAuthBloc.add(any()));
  });

  /// ---------------------------------------------------------------------------
  /// TEST 4 — Valid register dispatches event
  /// ---------------------------------------------------------------------------
  testWidgets('dispatches RegisterRequested on valid input', (tester) async {
    await pumpWithBlocs(tester);

    await tester.enterText(find.byType(TextFormField).at(0), 'newUser');
    await tester.enterText(find.byType(TextFormField).at(1), 'abcd1234');

    await tester.tap(find.widgetWithText(ElevatedButton, 'Sign Up'));
    await tester.pump();

    verify(() => mockAuthBloc.add(
          const RegisterRequested('newUser', 'abcd1234'),
        )).called(1);
  });

  /// ---------------------------------------------------------------------------
  /// TEST 5 — Loading Spinner
  /// ---------------------------------------------------------------------------
  testWidgets('shows loading spinner when isLoading = true', (tester) async {
    when(() => mockAuthBloc.state)
        .thenReturn(const AuthState(isLoading: true, isAuthenticated: false));

    await pumpWithBlocs(tester);

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  /// ---------------------------------------------------------------------------
  /// TEST 6 — Success triggers pop
  /// ---------------------------------------------------------------------------
  testWidgets('success triggers Navigator.pop()', (tester) async {
    whenListen(
      mockAuthBloc,
      Stream.fromIterable([
        const AuthState(isLoading: false, isAuthenticated: true),
      ]),
      initialState:
          const AuthState(isLoading: false, isAuthenticated: false),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: MultiBlocProvider(
          providers: [
            BlocProvider<AuthBloc>.value(value: mockAuthBloc),
            BlocProvider<ThemeBloc>.value(value: mockThemeBloc),
          ],
          child: const RegisterScreen(),
        ),
      ),
    );

    await tester.pump();
    await tester.pumpAndSettle();

    /// If popped → RegisterScreen should not be visible
    expect(find.byType(RegisterScreen), findsNothing);
  });

  /// ---------------------------------------------------------------------------
  /// TEST 7 — Theme toggle dispatches ToggleTheme
  /// ---------------------------------------------------------------------------
  testWidgets('theme toggle dispatches ToggleTheme', (tester) async {
    when(() => mockThemeBloc.state)
        .thenReturn(const ThemeState(ThemeMode.light)); // light → night icon

    await pumpWithBlocs(tester);

    final icon = find.byIcon(Icons.nightlight_round);
    expect(icon, findsOneWidget);

    await tester.tap(icon);
    await tester.pump();

    verify(() => mockThemeBloc.add(ToggleTheme())).called(1);
  });
}
