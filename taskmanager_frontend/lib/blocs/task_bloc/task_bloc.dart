import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:task_manager_app/blocs/task_bloc/task_event.dart';
import 'package:task_manager_app/blocs/task_bloc/task_state.dart';
import 'package:task_manager_app/services/task_service.dart';

class TaskBloc extends Bloc<TaskEvent, TaskState> {

  TaskBloc([TaskService? service])
      : _service = service ?? TaskService(),
        super(TaskState.initial()) {
    on<LoadTasks>(_onLoadTasks);
    on<AddTask>(_onAddTask);
    on<DeleteTask>(_onDeleteTask);
    on<UpdateTask>(_onUpdateTask);
  }
  final TaskService _service;

  Future<void> _onLoadTasks(LoadTasks event, Emitter<TaskState> emit) async {
    emit(state.copyWith(isLoading: true, errorMessage: ''));
    try {
      final tasks = await _service.fetchTasks();
      emit(state.copyWith(tasks: tasks, isLoading: false));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _onAddTask(AddTask event, Emitter<TaskState> emit) async {
    emit(state.copyWith(isLoading: true, errorMessage: ''));
    try {
      final newTask = await _service.addTask(event.title, event.description);
      final updatedList = [newTask, ...state.tasks];
      emit(state.copyWith(tasks: updatedList, isLoading: false));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _onDeleteTask(DeleteTask event, Emitter<TaskState> emit) async {
    emit(state.copyWith(isLoading: true, errorMessage: ''));
    try {
      await _service.deleteTask(event.id);
      final updatedList =
          state.tasks.where((t) => t.id != event.id).toList();
      emit(state.copyWith(tasks: updatedList, isLoading: false));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }

  Future<void> _onUpdateTask(UpdateTask event, Emitter<TaskState> emit) async {
    emit(state.copyWith(isLoading: true, errorMessage: ''));
    try {
      final updatedTask = await _service.updateTask(event.updatedTask);
      final updatedList = state.tasks.map((t) {
        return t.id == updatedTask.id ? updatedTask : t;
      }).toList();
      emit(state.copyWith(tasks: updatedList, isLoading: false));
    } catch (e) {
      emit(state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      ));
    }
  }
}
