import 'package:equatable/equatable.dart';
import 'package:task_manager_app/models/task_model.dart';

class TaskState extends Equatable {

  factory TaskState.initial() => const TaskState(tasks: [], isLoading: false);

  const TaskState({
    required this.tasks,
    required this.isLoading,
    this.errorMessage,
  });
  final List<TaskModel> tasks;
  final bool isLoading;
  final String? errorMessage;

  TaskState copyWith({
    List<TaskModel>? tasks,
    bool? isLoading,
    String? errorMessage,
  }) {
    return TaskState(
      tasks: tasks ?? this.tasks,
      isLoading: isLoading ?? this.isLoading,
      errorMessage: errorMessage,
    );
  }

  @override
  List<Object?> get props => [tasks, isLoading, errorMessage];
}
