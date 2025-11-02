import 'package:equatable/equatable.dart';
import 'package:task_manager_app/models/task_model.dart';

abstract class TaskEvent extends Equatable {
  const TaskEvent();

  @override
  List<Object?> get props => [];
}

class LoadTasks extends TaskEvent {}

class AddTask extends TaskEvent {

  const AddTask(this.title, this.description);
  final String title;
  final String description;

  @override
  List<Object?> get props => [title, description];
}

class DeleteTask extends TaskEvent {

  const DeleteTask(this.id);
  final int id;

  @override
  List<Object?> get props => [id];
}

class UpdateTask extends TaskEvent {

  const UpdateTask(this.updatedTask);
  final TaskModel updatedTask;

  @override
  List<Object?> get props => [updatedTask];
}
