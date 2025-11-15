import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';
import 'package:task_manager_app/features/tasks/presentation/home_screen.dart';
import 'package:task_manager_app/features/auth/presentation/register_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});
  static const routeName = '/login';

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _userCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _obscurePass = true;

  void _onLoginPressed(BuildContext context) {
    if (!_formKey.currentState!.validate()) return;
    final username = _userCtrl.text.trim();
    final password = _passCtrl.text.trim();
    context.read<AuthBloc>().add(LoginRequested(username, password));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      appBar: _buildAppBar(theme),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 400, minWidth: 280),
            child: _buildLoginCard(theme),
          ),
        ),
      ),
    );
  }

  AppBar _buildAppBar(ThemeData theme) {
    return AppBar(
      title: const Text('Login'),
      centerTitle: true,
      backgroundColor: theme.colorScheme.primary,
      foregroundColor: theme.colorScheme.onPrimary,
      elevation: 2,
      actions: [
        BlocBuilder<ThemeBloc, ThemeState>(
          builder: (context, themeState) {
            final isDarkMode = themeState.isDarkMode;
            return IconButton(
              icon: Icon(
                isDarkMode ? Icons.wb_sunny_outlined : Icons.nightlight_round,
                color: theme.colorScheme.onPrimary,
              ),
              tooltip: isDarkMode ? 'Light Mode' : 'Dark Mode',
              onPressed: () => context.read<ThemeBloc>().add(ToggleTheme()),
            );
          },
        ),
      ],
    );
  }

  Widget _buildLoginCard(ThemeData theme) {
    return Card(
      elevation: 6,
      color: theme.cardColor,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
        child: Form(
          key: _formKey,
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            _buildHeader(theme),
            const SizedBox(height: 28),
            _buildUsernameField(),
            const SizedBox(height: 18),
            _buildPasswordField(),
            const SizedBox(height: 28),
            _buildLoginButton(theme),
            const SizedBox(height: 16),
            _buildRegisterButton(theme),
          ]),
        ),
      ),
    );
  }

  Widget _buildHeader(ThemeData theme) {
    return Text(
      'Welcome',
      style: theme.textTheme.headlineSmall?.copyWith(
        fontWeight: FontWeight.bold,
        color: theme.colorScheme.primary,
      ),
    );
  }

  Widget _buildUsernameField() {
    return SizedBox(
      width: 280,
      child: TextFormField(
        controller: _userCtrl,
        textInputAction: TextInputAction.next,
        decoration: InputDecoration(
          labelText: 'Username',
          prefixIcon: const Icon(Icons.person_outline, size: 20),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
        ),
        validator: (v) =>
            (v == null || v.trim().isEmpty) ? 'Enter username' : null,
      ),
    );
  }

  Widget _buildPasswordField() {
    return SizedBox(
      width: 280,
      child: TextFormField(
        controller: _passCtrl,
        obscureText: _obscurePass,
        textInputAction: TextInputAction.done,
        onFieldSubmitted: (_) => _onLoginPressed(context),
        decoration: InputDecoration(
          labelText: 'Password',
          prefixIcon: const Icon(Icons.lock_outline, size: 20),
          suffixIcon: IconButton(
            icon: Icon(
              _obscurePass ? Icons.visibility_off : Icons.visibility,
              size: 20,
            ),
            onPressed: () => setState(() => _obscurePass = !_obscurePass),
          ),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
        ),
        validator: (v) => (v == null || v.isEmpty) ? 'Enter password' : null,
      ),
    );
  }

  Widget _buildLoginButton(ThemeData theme) {
    return BlocConsumer<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state.errorMessage != null) {
          Fluttertoast.showToast(msg: state.errorMessage!);
        }
        if (state.isAuthenticated) {
          Fluttertoast.showToast(msg: 'Login successful');
          Navigator.of(context).pushReplacement(
            MaterialPageRoute(builder: (_) => const HomeScreen()),
          );
        }
      },
      builder: (context, state) {
        if (state.isLoading) return const CircularProgressIndicator();
        return SizedBox(
          width: 160,
          height: 46,
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: theme.colorScheme.primary,
              foregroundColor: theme.colorScheme.onPrimary,
              textStyle: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            onPressed: () => _onLoginPressed(context),
            child: const Text('Login'),
          ),
        );
      },
    );
  }

  Widget _buildRegisterButton(ThemeData theme) {
    return TextButton(
      onPressed: () => Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => const RegisterScreen()),
      ),
      style: TextButton.styleFrom(
        foregroundColor: theme.colorScheme.primary.withValues(alpha: 0.9),
      ),
      child: const Text('Create an Account'),
    );
  }
}
