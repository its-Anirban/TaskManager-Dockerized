class LoginResponse {
  factory LoginResponse.fromMap(Map<String, dynamic> map) {
    return LoginResponse(token: map['token'] ?? '');
  }
  LoginResponse({required this.token});
  final String token;
}
