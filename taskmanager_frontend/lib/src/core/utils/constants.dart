import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

class Constants {
  static const String _localhostUrl = 'http://localhost:8080';
  static const String _androidUrl = 'http://10.0.2.2:8080';

  static String get baseUrl {
    if (kIsWeb) {
      // For Web builds (handled by Nginx proxy)
      return ''; // Use relative path so /api resolves correctly
    }

    if (Platform.isAndroid) {
      return _androidUrl;
    }

    if (Platform.isIOS || Platform.isWindows || Platform.isMacOS || Platform.isLinux) {
      return _localhostUrl;
    }

    // Fallback for any unhandled platform
    return _localhostUrl;
  }

  static const String tokenKey = 'auth_token';
}
