import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:task_manager_app/blocs/auth_bloc/auth_event.dart';
import 'package:task_manager_app/blocs/auth_bloc/auth_state.dart';
import 'package:task_manager_app/services/auth_service.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {

  AuthBloc([AuthService? authService])
      : _authService = authService ?? AuthService(),
        super(AuthState.initial()) {
    on<LoginRequested>(_onLoginRequested);
    on<RegisterRequested>(_onRegisterRequested);
    on<LogoutRequested>(_onLogoutRequested);
    on<CheckLoginStatus>(_onCheckLoginStatus);
  }
  final AuthService _authService;

  Future<void> _onLoginRequested(
      LoginRequested event, Emitter<AuthState> emit) async {
    emit(state.copyWith(isLoading: true));
    try {
      await _authService.login(event.username, event.password);
      emit(state.copyWith(isLoading: false, isAuthenticated: true));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _onRegisterRequested(
      RegisterRequested event, Emitter<AuthState> emit) async {
    emit(state.copyWith(isLoading: true));
    try {
      await _authService.register(event.username, event.password);
      emit(state.copyWith(isLoading: false, isAuthenticated: true));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _onLogoutRequested(
      LogoutRequested event, Emitter<AuthState> emit) async {
    await AuthService.clearToken();
    emit(AuthState.initial());
  }

  Future<void> _onCheckLoginStatus(
      CheckLoginStatus event, Emitter<AuthState> emit) async {
    final token = await AuthService.getToken();
    final loggedIn = token != null && token.isNotEmpty;
    emit(state.copyWith(isAuthenticated: loggedIn));
  }
}
