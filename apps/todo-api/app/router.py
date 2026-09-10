from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.repository import TodoRepository
from app.schemas import TodoCreate, TodoRead, TodoUpdate
from app.service import TodoService

router = APIRouter(prefix="/api/todos", tags=["todos"])


def get_service(db: Session = Depends(get_db)) -> TodoService:
    return TodoService(TodoRepository(db))


@router.get("", response_model=List[TodoRead])
def get_all_todos(
    completed: Optional[bool] = None,
    search: Optional[str] = None,
    service: TodoService = Depends(get_service),
):
    if search:
        return service.search_todos(search)
    if completed is not None:
        return service.get_todos_by_status(completed)
    return service.get_all_todos()


@router.get("/{todo_id}", response_model=TodoRead)
def get_todo_by_id(todo_id: int, service: TodoService = Depends(get_service)):
    todo = service.get_todo_by_id(todo_id)
    if todo is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    return todo


@router.post("", response_model=TodoRead, status_code=status.HTTP_201_CREATED)
def create_todo(todo: TodoCreate, service: TodoService = Depends(get_service)):
    return service.create_todo(todo)


@router.put("/{todo_id}", response_model=TodoRead)
def update_todo(todo_id: int, todo: TodoUpdate, service: TodoService = Depends(get_service)):
    updated = service.update_todo(todo_id, todo)
    if updated is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    return updated


@router.patch("/{todo_id}/toggle", response_model=TodoRead)
def toggle_todo_status(todo_id: int, service: TodoService = Depends(get_service)):
    toggled = service.toggle_todo_status(todo_id)
    if toggled is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    return toggled


@router.delete("/{todo_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_todo(todo_id: int, service: TodoService = Depends(get_service)):
    if not service.delete_todo(todo_id):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
