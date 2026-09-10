from typing import List, Optional

from app.models import Todo
from app.repository import TodoRepository
from app.schemas import TodoCreate, TodoUpdate


class TodoService:
    def __init__(self, todo_repository: TodoRepository):
        self.todo_repository = todo_repository

    def get_all_todos(self) -> List[Todo]:
        return self.todo_repository.find_all()

    def get_todo_by_id(self, todo_id: int) -> Optional[Todo]:
        return self.todo_repository.find_by_id(todo_id)

    def get_todos_by_status(self, completed: bool) -> List[Todo]:
        return self.todo_repository.find_by_completed(completed)

    def search_todos(self, keyword: str) -> List[Todo]:
        return self.todo_repository.find_by_title_containing_ignore_case(keyword)

    def create_todo(self, todo_data: TodoCreate) -> Todo:
        todo = Todo(
            title=todo_data.title,
            description=todo_data.description,
            completed=todo_data.completed,
        )
        return self.todo_repository.save(todo)

    def update_todo(self, todo_id: int, todo_details: TodoUpdate) -> Optional[Todo]:
        todo = self.todo_repository.find_by_id(todo_id)
        if todo is None:
            return None
        todo.title = todo_details.title
        todo.description = todo_details.description
        todo.completed = todo_details.completed
        return self.todo_repository.save(todo)

    def delete_todo(self, todo_id: int) -> bool:
        todo = self.todo_repository.find_by_id(todo_id)
        if todo is None:
            return False
        self.todo_repository.delete(todo)
        return True

    def toggle_todo_status(self, todo_id: int) -> Optional[Todo]:
        todo = self.todo_repository.find_by_id(todo_id)
        if todo is None:
            return None
        todo.completed = not todo.completed
        return self.todo_repository.save(todo)
