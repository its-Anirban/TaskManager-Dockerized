import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_event.dart';
import 'package:task_manager_app/features/auth/presentation/bloc/auth_state.dart';
import 'package:task_manager_app/features/theme/presentation/theme_bloc.dart';
import 'package:task_manager_app/features/theme/presentation/theme_event.dart';
import 'package:task_manager_app/features/theme/presentation/theme_state.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});
  static const routeName = '/register';

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _userCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _obscurePass = true;

  void _onRegisterPressed(BuildContext context) {
    if (!_formKey.currentState!.validate()) return;
    final username = _userCtrl.text.trim();
    final password = _passCtrl.text.trim();
    context.read<AuthBloc>().add(RegisterRequested(username, password));
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
            constraints: const BoxConstraints(maxWidth: 420, minWidth: 280),
            child: _buildRegisterCard(theme),
          ),
        ),
      ),
    );
  }

  // ---- UI building methods ----

  AppBar _buildAppBar(ThemeData theme) {
    return AppBar(
      title: const Text('Create Account'),
      centerTitle: true,
      backgroundColor: theme.colorScheme.primary,
      foregroundColor: theme.colorScheme.onPrimary,
      elevation: 2,
      actions: [
        BlocBuilder<ThemeBloc, ThemeState>(
          builder: (context, state) {
            final isDarkMode = state.isDarkMode;
            return IconButton(
              tooltip: isDarkMode ? 'Light Mode' : 'Dark Mode',
              icon: Icon(
                isDarkMode ? Icons.wb_sunny_outlined : Icons.nightlight_round,
              ),
              onPressed: () => context.read<ThemeBloc>().add(ToggleTheme()),
            );
          },
        ),
      ],
    );
  }

  Widget _buildRegisterCard(ThemeData theme) {
    return Card(
      elevation: 6,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 30),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildHeader(theme),
              const SizedBox(height: 24),
              _buildUsernameField(),
              const SizedBox(height: 16),
              _buildPasswordField(),
              const SizedBox(height: 24),
              _buildRegisterButton(theme),
              const SizedBox(height: 16),
              _buildBackButton(theme),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(ThemeData theme) {
    return Text(
      'Register',
      style: theme.textTheme.headlineSmall?.copyWith(
        fontWeight: FontWeight.bold,
        color: theme.colorScheme.primary,
      ),
    );
  }

  Widget _buildUsernameField() {
    return TextFormField(
      controller: _userCtrl,
      textInputAction: TextInputAction.next,
      decoration: InputDecoration(
        labelText: 'Username',
        prefixIcon: const Icon(Icons.person_outline, size: 20),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
      ),
      validator: (v) =>
          (v == null || v.trim().isEmpty) ? 'Enter username' : null,
    );
  }

  Widget _buildPasswordField() {
    return TextFormField(
      controller: _passCtrl,
      obscureText: _obscurePass,
      textInputAction: TextInputAction.done,
      onFieldSubmitted: (_) => _onRegisterPressed(context),
      decoration: InputDecoration(
        labelText: 'Password',
        prefixIcon: const Icon(Icons.lock_outline, size: 20),
        suffixIcon: IconButton(
          icon: Icon(
            _obscurePass
                ? Icons.visibility_off_outlined
                : Icons.visibility_outlined,
          ),
          onPressed: () => setState(() => _obscurePass = !_obscurePass),
        ),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
      ),
      validator: (v) => (v == null || v.length < 4)
          ? 'Password too short'
          : null,
    );
  }

  Widget _buildRegisterButton(ThemeData theme) {
    return BlocConsumer<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state.errorMessage != null) {
          Fluttertoast.showToast(
            msg: state.errorMessage!,
            gravity: ToastGravity.BOTTOM,
          );
        }
        if (state.isAuthenticated) {
          Fluttertoast.showToast(
            msg: 'Registration successful. Please login.',
            gravity: ToastGravity.BOTTOM,
          );
          Navigator.of(context).pop();
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
            onPressed: () => _onRegisterPressed(context),
            child: const Text('Sign Up'),
          ),
        );
      },
    );
  }

  Widget _buildBackButton(ThemeData theme) {
    return TextButton(
      onPressed: () => Navigator.of(context).pop(),
      style: TextButton.styleFrom(
        foregroundColor: theme.colorScheme.primary,
      ),
      child: const Text('Back to Login'),
    );
  }
}
