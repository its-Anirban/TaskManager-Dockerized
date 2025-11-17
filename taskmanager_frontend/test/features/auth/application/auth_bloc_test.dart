import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/auth/data/auth_response.dart';
import 'package:task_manager_app/features/auth/data/auth_service.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/src/core/utils/constants.dart';

class MockAuthService extends Mock implements AuthService {}

class FakeLoginRequested extends Fake implements LoginRequested {}
class FakeRegisterRequested extends Fake implements RegisterRequested {}
class FakeLogoutRequested extends Fake implements LogoutRequested {}
class FakeCheckLoginStatus extends Fake implements CheckLoginStatus {}

void main() {
  late MockAuthService mockAuthService;

  setUpAll(() {
    registerFallbackValue(FakeLoginRequested());
    registerFallbackValue(FakeRegisterRequested());
    registerFallbackValue(FakeLogoutRequested());
    registerFallbackValue(FakeCheckLoginStatus());
  });

  setUp(() {
    mockAuthService = MockAuthService();
    SharedPreferences.setMockInitialValues({});
  });

  group('AuthBloc Tests', () {

    blocTest<AuthBloc, AuthState>(
      'emits [loading, authenticated] on successful login',
      build: () {
        when(() => mockAuthService.login('user', 'pass'))
            .thenAnswer((_) async => LoginResponse(token: 'abc123'));
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(const LoginRequested('user', 'pass')),
      expect: () => [
        const AuthState(isLoading: true, isAuthenticated: false),
        const AuthState(isLoading: false, isAuthenticated: true),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [loading, error] on login failure',
      build: () {
        when(() => mockAuthService.login('user', 'pass'))
            .thenThrow(Exception('Login failed'));
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(const LoginRequested('user', 'pass')),
      expect: () => [
        const AuthState(isLoading: true, isAuthenticated: false),
        const AuthState(
          isLoading: false,
          isAuthenticated: false,
          errorMessage: 'Exception: Login failed',
        ),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [loading, authenticated] on successful register',
      build: () {
        when(() => mockAuthService.register('u', 'p'))
            .thenAnswer((_) async {});
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(const RegisterRequested('u', 'p')),
      expect: () => [
        const AuthState(isLoading: true, isAuthenticated: false),
        const AuthState(isLoading: false, isAuthenticated: true),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [loading, error] on register failure',
      build: () {
        when(() => mockAuthService.register('u', 'p'))
            .thenThrow(Exception('Registration failed'));
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(const RegisterRequested('u', 'p')),
      expect: () => [
        const AuthState(isLoading: true, isAuthenticated: false),
        const AuthState(
          isLoading: false,
          isAuthenticated: false,
          errorMessage: 'Exception: Registration failed',
        ),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'logout resets state and clears token',
      build: () {
        SharedPreferences.setMockInitialValues({
          Constants.tokenKey: 'abc123',
        });
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(LogoutRequested()),
      expect: () => [
        const AuthState(isLoading: false, isAuthenticated: false),
      ],
      verify: (_) async {
        final prefs = await SharedPreferences.getInstance();
        expect(prefs.getString(Constants.tokenKey), isNull);
      },
    );

    blocTest<AuthBloc, AuthState>(
      'CheckLoginStatus emits authenticated=true when token exists',
      build: () {
        SharedPreferences.setMockInitialValues({
          Constants.tokenKey: 'valid_token',
        });
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(CheckLoginStatus()),
      expect: () => [
        const AuthState(isLoading: false, isAuthenticated: true),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'CheckLoginStatus emits authenticated=false when token missing',
      build: () {
        SharedPreferences.setMockInitialValues({});
        return AuthBloc(mockAuthService);
      },
      act: (bloc) => bloc.add(CheckLoginStatus()),
      expect: () => [
        const AuthState(isLoading: false, isAuthenticated: false),
      ],
    );

  });
}
