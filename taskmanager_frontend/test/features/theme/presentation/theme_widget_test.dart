import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

/// A simple widget that displays a theme toggle icon.
/// Many of your screens use the same logic, so testing once is enough.
class _TestThemeWidget extends StatelessWidget {
  const _TestThemeWidget();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ThemeBloc, ThemeState>(
      builder: (_, state) {
        final isDark = state.isDarkMode;
        return IconButton(
          key: const Key('theme_toggle_button'),
          icon: Icon(
            isDark ? Icons.wb_sunny_outlined : Icons.nightlight_round,
            key: const Key('theme_icon'),
          ),
          onPressed: () => context.read<ThemeBloc>().add(ToggleTheme()),
        );
      },
    );
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  Widget wrapWithThemeBloc() {
    return MaterialApp(
      home: BlocProvider(
        create: (_) => ThemeBloc(),
        child: const Scaffold(
          body: Center(
            child: _TestThemeWidget(),
          ),
        ),
      ),
    );
  }

  testWidgets('Initial icon should be dark-mode icon (nightlight_round)', (tester) async {
    await tester.pumpWidget(wrapWithThemeBloc());

    // Allow ThemeBloc constructor LoadTheme() event to process
    await tester.pump(const Duration(milliseconds: 50));

    final icon = tester.widget<Icon>(find.byKey(const Key('theme_icon')));
    expect(icon.icon, Icons.nightlight_round);
  });

  testWidgets('Tapping toggle button switches to light-mode icon (sun icon)', (tester) async {
    await tester.pumpWidget(wrapWithThemeBloc());
    await tester.pump(const Duration(milliseconds: 50));

    // Tap toggle
    await tester.tap(find.byKey(const Key('theme_toggle_button')));
    await tester.pump(const Duration(milliseconds: 50));

    final icon = tester.widget<Icon>(find.byKey(const Key('theme_icon')));
    expect(icon.icon, Icons.wb_sunny_outlined);
  });

  testWidgets('If SharedPreferences says darkMode=true, initial icon is sun icon', (tester) async {
    SharedPreferences.setMockInitialValues({'isDarkMode': true});

    await tester.pumpWidget(wrapWithThemeBloc());
    await tester.pump(const Duration(milliseconds: 50));

    final icon = tester.widget<Icon>(find.byKey(const Key('theme_icon')));
    expect(icon.icon, Icons.wb_sunny_outlined);
  });
}
