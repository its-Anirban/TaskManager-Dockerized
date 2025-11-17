import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('ThemeBloc Tests', () {
    setUp(() {
      // Reset shared prefs before every test
      SharedPreferences.setMockInitialValues({});
    });

    test('initial state is ThemeMode.light', () {
      final bloc = ThemeBloc();
      expect(bloc.state, const ThemeState(ThemeMode.light));
      bloc.close();
    });

    test('LoadTheme loads dark mode from SharedPreferences', () async {
      SharedPreferences.setMockInitialValues({'isDarkMode': true});

      final bloc = ThemeBloc();

      // Give bloc time to process LoadTheme added in constructor
      await Future.delayed(const Duration(milliseconds: 50));

      expect(bloc.state.themeMode, ThemeMode.dark);
      expect(bloc.state.isDarkMode, true);

      bloc.close();
    });

    test('ToggleTheme switches from light -> dark', () async {
      final bloc = ThemeBloc();

      bloc.add(ToggleTheme());

      await Future.delayed(const Duration(milliseconds: 50));

      expect(bloc.state.themeMode, ThemeMode.dark);
      expect(bloc.state.isDarkMode, true);

      bloc.close();
    });

    test('ToggleTheme switches from dark -> light', () async {
      SharedPreferences.setMockInitialValues({'isDarkMode': true});

      final bloc = ThemeBloc();

      // wait constructor LoadTheme
      await Future.delayed(const Duration(milliseconds: 50));
      expect(bloc.state.themeMode, ThemeMode.dark);

      bloc.add(ToggleTheme());
      await Future.delayed(const Duration(milliseconds: 50));

      expect(bloc.state.themeMode, ThemeMode.light);
      expect(bloc.state.isDarkMode, false);

      bloc.close();
    });
  });
}
