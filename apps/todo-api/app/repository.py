from typing import List, Optional

from sqlalchemy.orm import Session

from app.models import Todo


class TodoRepository:
    def __init__(self, db: Session):
        self.db = db

    def find_all(self) -> List[Todo]:
        return self.db.query(Todo).all()

    def find_by_id(self, todo_id: int) -> Optional[Todo]:
        return self.db.query(Todo).filter(Todo.id == todo_id).first()

    def find_by_completed(self, completed: bool) -> List[Todo]:
        return self.db.query(Todo).filter(Todo.completed == completed).all()

    def find_by_title_containing_ignore_case(self, keyword: str) -> List[Todo]:
        return self.db.query(Todo).filter(Todo.title.ilike(f"%{keyword}%")).all()

    def save(self, todo: Todo) -> Todo:
        self.db.add(todo)
        self.db.commit()
        self.db.refresh(todo)
        return todo

    def delete(self, todo: Todo) -> None:
        self.db.delete(todo)
        self.db.commit()
