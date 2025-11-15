import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/tasks/domain/task_bloc.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_event.dart';
import 'package:task_manager_app/features/tasks/presentation/bloc/task_state.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';
import 'package:task_manager_app/features/auth/presentation/login_screen.dart';
import 'package:task_manager_app/features/tasks/presentation/add_task_dialog.dart';
import 'package:task_manager_app/features/tasks/presentation/task_card.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  static const routeName = '/home';

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaskBloc>().add(LoadTasks());
    });
  }

  void _openAdd() {
    showDialog(
      context: context,
      builder: (_) => AddTaskDialog(
        onDone: (title, desc) async {
          context.read<TaskBloc>().add(AddTask(title, desc));
          Fluttertoast.showToast(msg: 'Task added');
        },
      ),
    );
  }

  void _deleteTask(int id) {
    context.read<TaskBloc>().add(DeleteTask(id));
    Fluttertoast.showToast(msg: 'Task deleted');
  }

  void _logout() {
    context.read<AuthBloc>().add(LogoutRequested());
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
    );
  }

  void _refreshTasks() => context.read<TaskBloc>().add(LoadTasks());

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return BlocBuilder<ThemeBloc, ThemeState>(
      builder: (context, themeState) {
        return Scaffold(
          backgroundColor: theme.scaffoldBackgroundColor,
          appBar: _buildAppBar(theme, themeState),
          body: _buildBody(theme),
          floatingActionButton: _buildFab(theme),
        );
      },
    );
  }

  AppBar _buildAppBar(ThemeData theme, ThemeState themeState) {
    final isDarkMode = themeState.isDarkMode;
    return AppBar(
      centerTitle: true,
      backgroundColor: theme.colorScheme.primary,
      foregroundColor: theme.colorScheme.onPrimary,
      elevation: 2,
      title: const Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.task_alt_rounded, size: 24),
          SizedBox(width: 8),
          Text(
            'Task Manager',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
        ],
      ),
      actions: [
        IconButton(
          tooltip: isDarkMode ? 'Light Mode' : 'Dark Mode',
          icon: Icon(
            isDarkMode ? Icons.wb_sunny_outlined : Icons.nightlight_round,
          ),
          onPressed: () => context.read<ThemeBloc>().add(ToggleTheme()),
        ),
        IconButton(
          onPressed: _logout,
          icon: const Icon(Icons.logout),
          tooltip: 'Logout',
        ),
      ],
    );
  }

  Widget _buildBody(ThemeData theme) {
    return BlocBuilder<TaskBloc, TaskState>(
      builder: (context, state) {
        if (state.isLoading) return const Center(child: CircularProgressIndicator());
        if ((state.errorMessage ?? '').isNotEmpty) {
          return _buildErrorMessage(theme, state.errorMessage!);
        }
        if (state.tasks.isEmpty) {
          return _buildEmptyMessage(theme);
        }
        return _buildTaskGrid(state);
      },
    );
  }

  Widget _buildErrorMessage(ThemeData theme, String message) {
    return Center(
      child: Text(
        'Error: $message',
        style: TextStyle(
          color: theme.colorScheme.error,
          fontWeight: FontWeight.w500,
          fontSize: 16,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildEmptyMessage(ThemeData theme) {
    return Center(
      child: Text(
        'No tasks yet.\nTap “Add Task” to create one.',
        style: TextStyle(
          color: theme.colorScheme.onSurface.withValues(alpha: 0.7),
          fontSize: 16,
          fontWeight: FontWeight.w500,
          height: 1.4,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  Widget _buildTaskGrid(TaskState state) {
    return RefreshIndicator(
      onRefresh: () async {
        _refreshTasks();
        return Future.value();
      },
      child: MasonryGridView.count(
        padding: const EdgeInsets.all(16),
        crossAxisCount: MediaQuery.of(context).size.width < 600 ? 2 : 3,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        physics: const BouncingScrollPhysics(
          parent: AlwaysScrollableScrollPhysics(),
        ),
        itemCount: state.tasks.length,
        itemBuilder: (context, i) {
          final t = state.tasks[i];
          return AnimatedSwitcher(
            duration: const Duration(milliseconds: 250),
            child: TaskCard(
              key: ValueKey(t.id ?? t.title),
              task: t,
              onDelete: () {
                if (t.id != null) _deleteTask(t.id!);
              },
              onEdit: _refreshTasks,
            ),
          );
        },
      ),
    );
  }

  FloatingActionButton _buildFab(ThemeData theme) {
    return FloatingActionButton.extended(
      onPressed: _openAdd,
      backgroundColor: theme.colorScheme.primary,
      foregroundColor: theme.colorScheme.onPrimary,
      elevation: 6,
      icon: const Icon(Icons.add),
      label: const Text(
        'Add Task',
        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
      ),
    );
  }
}
