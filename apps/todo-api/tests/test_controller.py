from datetime import datetime
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.models import Todo
from app.router import get_service


@pytest.fixture()
def todo_service():
    return MagicMock()


@pytest.fixture()
def client(todo_service):
    app.dependency_overrides[get_service] = lambda: todo_service
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


@pytest.fixture()
def sample_todo():
    return Todo(
        id=1,
        title="Test Todo",
        description="Test Description",
        completed=False,
        created_at=datetime.now(),
        updated_at=datetime.now(),
    )


def test_get_all_todos_should_return_todo_list(client, todo_service, sample_todo):
    todo_service.get_all_todos.return_value = [sample_todo]

    response = client.get("/api/todos")

    assert response.status_code == 200
    body = response.json()
    assert len(body) == 1
    assert body[0]["title"] == "Test Todo"
    todo_service.get_all_todos.assert_called_once()


def test_get_all_todos_with_completed_filter_should_return_filtered_list(client, todo_service, sample_todo):
    todo_service.get_todos_by_status.return_value = [sample_todo]

    response = client.get("/api/todos", params={"completed": "true"})

    assert response.status_code == 200
    assert len(response.json()) == 1
    todo_service.get_todos_by_status.assert_called_once_with(True)


def test_get_all_todos_with_search_filter_should_return_search_results(client, todo_service, sample_todo):
    todo_service.search_todos.return_value = [sample_todo]

    response = client.get("/api/todos", params={"search": "Test"})

    assert response.status_code == 200
    assert len(response.json()) == 1
    todo_service.search_todos.assert_called_once_with("Test")


def test_get_todo_by_id_when_exists_should_return_todo(client, todo_service, sample_todo):
    todo_service.get_todo_by_id.return_value = sample_todo

    response = client.get("/api/todos/1")

    assert response.status_code == 200
    body = response.json()
    assert body["title"] == "Test Todo"
    assert body["description"] == "Test Description"
    todo_service.get_todo_by_id.assert_called_once_with(1)


def test_get_todo_by_id_when_not_exists_should_return_404(client, todo_service):
    todo_service.get_todo_by_id.return_value = None

    response = client.get("/api/todos/999")

    assert response.status_code == 404
    todo_service.get_todo_by_id.assert_called_once_with(999)


def test_create_todo_with_valid_data_should_return_created(client, todo_service, sample_todo):
    todo_service.create_todo.return_value = sample_todo

    response = client.post("/api/todos", json={"title": "Test Todo", "description": "Test Description"})

    assert response.status_code == 201
    assert response.json()["title"] == "Test Todo"
    todo_service.create_todo.assert_called_once()


def test_create_todo_with_invalid_data_should_return_422(client, todo_service):
    response = client.post("/api/todos", json={"title": ""})

    assert response.status_code == 422
    todo_service.create_todo.assert_not_called()


def test_update_todo_when_exists_should_return_updated_todo(client, todo_service, sample_todo):
    todo_service.update_todo.return_value = sample_todo

    response = client.put("/api/todos/1", json={"title": "Test Todo", "description": "Test Description"})

    assert response.status_code == 200
    assert response.json()["title"] == "Test Todo"
    todo_service.update_todo.assert_called_once()


def test_update_todo_when_not_exists_should_return_404(client, todo_service, sample_todo):
    todo_service.update_todo.return_value = None

    response = client.put("/api/todos/999", json={"title": "Test Todo", "description": "Test Description"})

    assert response.status_code == 404
    todo_service.update_todo.assert_called_once()


def test_toggle_todo_status_when_exists_should_return_updated_todo(client, todo_service, sample_todo):
    sample_todo.completed = True
    todo_service.toggle_todo_status.return_value = sample_todo

    response = client.patch("/api/todos/1/toggle")

    assert response.status_code == 200
    todo_service.toggle_todo_status.assert_called_once_with(1)


def test_delete_todo_when_exists_should_return_204(client, todo_service):
    todo_service.delete_todo.return_value = True

    response = client.delete("/api/todos/1")

    assert response.status_code == 204
    todo_service.delete_todo.assert_called_once_with(1)


def test_delete_todo_when_not_exists_should_return_404(client, todo_service):
    todo_service.delete_todo.return_value = False

    response = client.delete("/api/todos/999")

    assert response.status_code == 404
    todo_service.delete_todo.assert_called_once_with(999)
