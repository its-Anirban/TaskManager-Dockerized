// test/features/task/presentation/widgets/task_card_test.dart

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_app/features/tasks/data/task_model.dart';
import 'package:task_manager_app/features/tasks/presentation/task_card.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('TaskCard Widget Tests', () {
    /// ----------------------------------------------------------------------
    /// 1. Renders title & description
    /// ----------------------------------------------------------------------
    testWidgets('renders title and description', (tester) async {
      const task = TaskModel(
        id: 1,
        title: 'My Task',
        description: 'Task description',
      );

      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: TaskCard(task: task),
          ),
        ),
      );

      expect(find.text('My Task'), findsOneWidget);
      expect(find.text('Task description'), findsOneWidget);
    });

    /// ----------------------------------------------------------------------
    /// 2. Deleting → shows confirmation dialog
    /// ----------------------------------------------------------------------
    testWidgets('tapping delete icon shows confirmation dialog',
        (tester) async {
      const task = TaskModel(
        id: 2,
        title: 'Delete Me',
        description: '',
      );

      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: TaskCard(task: task),
          ),
        ),
      );

      final deleteBtn = find.byIcon(Icons.delete_outline);
      expect(deleteBtn, findsOneWidget);

      await tester.tap(deleteBtn);
      await tester.pumpAndSettle();

      expect(find.text('Confirm Deletion'), findsOneWidget);
      expect(
        find.text('Are you sure you want to delete this task?'),
        findsOneWidget,
      );
    });

    /// ----------------------------------------------------------------------
    /// 3. Confirming delete calls onDelete()
    /// ----------------------------------------------------------------------
    testWidgets('confirming delete calls onDelete callback', (tester) async {
      bool deleteCalled = false;

      const task = TaskModel(
        id: 3,
        title: 'Test',
        description: '',
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TaskCard(
              task: task,
              onDelete: () => deleteCalled = true,
            ),
          ),
        ),
      );

      await tester.tap(find.byIcon(Icons.delete_outline));
      await tester.pumpAndSettle();

      await tester.tap(find.text('OK'));
      await tester.pumpAndSettle();

      expect(deleteCalled, isTrue);
    });

    /// ----------------------------------------------------------------------
    /// 4. Edit icon opens edit dialog (do NOT click Save)
    /// ----------------------------------------------------------------------
    testWidgets(
        'edit icon opens edit dialog without triggering network (no Save press)',
        (tester) async {
      const task = TaskModel(
        id: 4,
        title: 'Edit Me',
        description: 'desc',
      );

      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: TaskCard(task: task),
          ),
        ),
      );

      final editBtn = find.byIcon(Icons.edit_outlined);
      expect(editBtn, findsOneWidget);

      await tester.tap(editBtn);
      await tester.pumpAndSettle();

      // Dialog should appear
      expect(find.text('Edit Task'), findsOneWidget);

      // Two text fields: Title + Description
      expect(find.byType(TextField), findsNWidgets(2));

      // DO NOT tap Save (would call TaskService.updateTask → network)
      expect(find.text('Save'), findsOneWidget);
    });
  });
}
