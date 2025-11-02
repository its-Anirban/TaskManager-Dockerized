class TaskModel {
  factory TaskModel.fromMap(Map<String, dynamic> map) {
    return TaskModel(
      id: map['id'] != null ? (map['id'] as num).toInt() : null,
      title: map['title'] ?? '',
      description: map['description'] ?? '',
      completed: map['completed'] ?? false,
    );
  }

  const TaskModel({
    this.id,
    required this.title,
    required this.description,
    this.completed = false,
  });
  final int? id;
  final String title;
  final String description;
  final bool completed;

  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      'title': title,
      'description': description,
      'completed': completed,
    };
    if (id != null) {
      map['id'] = id;
    }
    return map;
  }
}
