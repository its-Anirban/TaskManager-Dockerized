import 'package:equatable/equatable.dart';

abstract class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => [];
}

class LoginRequested extends AuthEvent {

  const LoginRequested(this.username, this.password);
  final String username;
  final String password;

  @override
  List<Object?> get props => [username, password];
}

class RegisterRequested extends AuthEvent {

  const RegisterRequested(this.username, this.password);
  final String username;
  final String password;

  @override
  List<Object?> get props => [username, password];
}

class LogoutRequested extends AuthEvent {}

class CheckLoginStatus extends AuthEvent {}
