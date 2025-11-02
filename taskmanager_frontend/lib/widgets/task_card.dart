import 'package:flutter/material.dart';
import 'package:task_manager_app/models/task_model.dart';
import 'package:task_manager_app/services/task_service.dart';

class TaskCard extends StatelessWidget {
  const TaskCard({super.key, required this.task, this.onDelete, this.onEdit});

  final TaskModel task;
  final VoidCallback? onDelete;
  final VoidCallback? onEdit;

  static final List<Color> _cardColors = [
    const Color(0xFFFFF9C4), // pastel yellow
    const Color(0xFFC8E6C9), // pastel green
    const Color(0xFFBBDEFB), // pastel blue
    const Color(0xFFFFCDD2), // pastel red
    const Color(0xFFD1C4E9), // pastel purple
    const Color(0xFFFFE0B2), // pastel orange
  ];

  Color get randomColor {
    final index = (task.id ?? task.title.hashCode).abs() % _cardColors.length;
    return _cardColors[index];
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final base = randomColor;
    final alpha = isDark ? 0.65 : 0.95;
    final bgColor = Color.fromARGB(
      (alpha * 255).round(),
      (base.r * 255).round() & 0xff,
      (base.g * 255).round() & 0xff,
      (base.b * 255).round() & 0xff,
    );

    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      curve: Curves.easeInOut,
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Color.fromARGB(
              ((isDark ? 0.3 : 0.08) * 255).round(),
              0,
              0,
              0,
            ),
            blurRadius: 6,
            offset: const Offset(0, 3),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          splashColor: theme.colorScheme.primary.withValues(alpha: 0.12),
          highlightColor: theme.colorScheme.primary.withValues(alpha: 0.06),
          onLongPress: onDelete,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Title + action buttons
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Text(
                        task.title,
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          fontSize: 16,
                          color: isDark ? Colors.white : Colors.black87,
                        ),
                        softWrap: true,
                      ),
                    ),
                    // Edit icon
                    IconButton(
                      icon: Icon(
                        Icons.edit_outlined,
                        size: 20,
                        color: isDark
                            ? Colors.white70
                            : const Color.fromARGB(160, 0, 0, 0),
                      ),
                      onPressed: () => _showEditDialog(context),
                      tooltip: 'Edit Task',
                    ),
                    // Delete icon
                    IconButton(
                      icon: Icon(
                        Icons.delete_outline,
                        size: 20,
                        color: isDark
                            ? Colors.white70
                            : const Color.fromARGB(160, 0, 0, 0),
                      ),
                      onPressed: onDelete,
                      tooltip: 'Delete Task',
                    ),
                  ],
                ),

                const SizedBox(height: 6),

                // Description
                if (task.description.trim().isNotEmpty)
                  Text(
                    task.description,
                    style: TextStyle(
                      color: isDark ? Colors.white70 : Colors.black87,
                      height: 1.25,
                      fontSize: 14,
                    ),
                    softWrap: true,
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showEditDialog(BuildContext context) {
    final titleController = TextEditingController(text: task.title);
    final descController = TextEditingController(text: task.description);

    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          title: const Text('Edit Task'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: titleController,
                decoration: const InputDecoration(labelText: 'Title'),
              ),
              TextField(
                controller: descController,
                decoration: const InputDecoration(labelText: 'Description'),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                final service = TaskService();
                final updatedTask = TaskModel(
                  id: task.id,
                  title: titleController.text.trim(),
                  description: descController.text.trim(),
                );

                // Close dialog immediately BEFORE async call
                Navigator.pop(ctx);

                try {
                  await service.updateTask(updatedTask);

                  // Use parent context safely after await
                  if (context.mounted) {
                    onEdit?.call(); // trigger refresh
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Task updated successfully'),
                      ),
                    );
                  }
                } catch (e) {
                  if (context.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Error updating task: $e')),
                    );
                  }
                }
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );
  }
}
