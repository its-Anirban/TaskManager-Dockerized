import 'package:equatable/equatable.dart';

class AuthState extends Equatable {

  const AuthState({
    required this.isLoading,
    required this.isAuthenticated,
    this.errorMessage,
  });

  factory AuthState.initial() =>
      const AuthState(isLoading: false, isAuthenticated: false);
  final bool isLoading;
  final bool isAuthenticated;
  final String? errorMessage;

  AuthState copyWith({
    bool? isLoading,
    bool? isAuthenticated,
    String? errorMessage,
  }) {
    return AuthState(
      isLoading: isLoading ?? this.isLoading,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      errorMessage: errorMessage,
    );
  }

  @override
  List<Object?> get props => [isLoading, isAuthenticated, errorMessage];
}
