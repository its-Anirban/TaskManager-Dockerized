import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:task_manager_app/blocs/auth_bloc/auth_bloc.dart';
import 'package:task_manager_app/blocs/task_bloc/task_bloc.dart';
import 'package:task_manager_app/blocs/theme_bloc/theme_bloc.dart';
import 'package:task_manager_app/blocs/theme_bloc/theme_event.dart';
import 'package:task_manager_app/blocs/theme_bloc/theme_state.dart';
import 'package:task_manager_app/screens/home_screen.dart';
import 'package:task_manager_app/screens/login_screen.dart';
import 'package:task_manager_app/services/auth_service.dart';
import 'package:task_manager_app/services/task_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const TaskManagerApp());
}

class TaskManagerApp extends StatelessWidget {
  const TaskManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        // AuthBloc handles login, registration, logout
        BlocProvider<AuthBloc>(create: (_) => AuthBloc(AuthService())),

        // TaskBloc handles CRUD operations for tasks
        BlocProvider<TaskBloc>(create: (_) => TaskBloc(TaskService())),

        // ThemeBloc manages light/dark mode persistence
        BlocProvider<ThemeBloc>(create: (_) => ThemeBloc()..add(LoadTheme())),
      ],
      child: BlocBuilder<ThemeBloc, ThemeState>(
        builder: (context, state) {
          final isDark = state.isDarkMode;

          return MaterialApp(
            title: 'Task Manager',
            debugShowCheckedModeBanner: false,
            themeMode: isDark ? ThemeMode.dark : ThemeMode.light,

            // ------------------- LIGHT THEME -------------------
            theme: ThemeData(
              useMaterial3: true,
              colorScheme: ColorScheme.fromSeed(seedColor: Colors.blueAccent),
              scaffoldBackgroundColor: const Color(0xFFF5F6FA),
              cardColor: Colors.white,
              appBarTheme: const AppBarTheme(
                backgroundColor: Colors.blueAccent,
                foregroundColor: Colors.white,
                titleTextStyle: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ),
              ),
              floatingActionButtonTheme: const FloatingActionButtonThemeData(
                backgroundColor: Colors.blueAccent,
                foregroundColor: Colors.white,
              ),
            ),

            // ------------------- DARK THEME  -------------------
            darkTheme: ThemeData(
              useMaterial3: true,
              colorScheme: ColorScheme.fromSeed(
                seedColor: Colors.blueAccent,
                brightness: Brightness.dark,
                surface: const Color(0xFF1C1F26),
                primary: const Color(0xFF3D8BFF),
                secondary: const Color(0xFF7BAAF7),
              ),
              scaffoldBackgroundColor: const Color(0xFF0E1117),
              cardColor: const Color(0xFF1C1F26),
              dialogTheme: const DialogThemeData(
                backgroundColor: Color(0xFF23272F),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(16)),
                ),
              ),

              appBarTheme: const AppBarTheme(
                backgroundColor: Color(0xFF1C1F26),
                foregroundColor: Colors.white,
                titleTextStyle: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ),
              ),
              floatingActionButtonTheme: const FloatingActionButtonThemeData(
                backgroundColor: Color(0xFF3D8BFF),
                foregroundColor: Colors.white,
              ),
              textTheme: const TextTheme(
                bodyMedium: TextStyle(color: Color(0xFFE6E6E6)),
                bodyLarge: TextStyle(color: Color(0xFFF0F0F0)),
              ),
              inputDecorationTheme: InputDecorationTheme(
                filled: true,
                fillColor: const Color(0xFF2C313A),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: BorderSide.none,
                ),
                labelStyle: const TextStyle(color: Color(0xFF9AA4B1)),
                hintStyle: const TextStyle(color: Color(0xFF9AA4B1)),
              ),
            ),

            // ------------------- INITIAL SCREEN & ROUTES -------------------
            home: const LoginScreen(),
            routes: {
              HomeScreen.routeName: (_) => const HomeScreen(),
              LoginScreen.routeName: (_) => const LoginScreen(),
            },
          );
        },
      ),
    );
  }
}
