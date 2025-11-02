import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

class Constants {
  static String get baseUrl {
    // For Web (Flutter web build running in browser, behind Nginx)
    // Use relative path because Nginx will proxy /api to backend:8080
    if (kIsWeb) {
      return ''; // use relative path
    }

    // For Android Emulator
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8080';
    }

    // For iOS Simulator
    if (Platform.isIOS) {
      return 'http://localhost:8080';
    }

    // For Desktop (Windows, macOS, Linux)
    if (Platform.isWindows || Platform.isMacOS || Platform.isLinux) {
      return 'http://localhost:8080';
    }

    // Default fallback
    return 'http://localhost:8080';
  }

  static const String tokenKey = 'auth_token';
}
