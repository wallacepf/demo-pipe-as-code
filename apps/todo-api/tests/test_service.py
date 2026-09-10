from unittest.mock import MagicMock

import pytest

from app.models import Todo
from app.schemas import TodoCreate, TodoUpdate
from app.service import TodoService


@pytest.fixture()
def repository():
    return MagicMock()


@pytest.fixture()
def service(repository):
    return TodoService(repository)


@pytest.fixture()
def sample_todo():
    return Todo(id=1, title="Test Todo", description="Test Description", completed=False)


def test_get_all_todos_should_return_all_todos(service, repository, sample_todo):
    repository.find_all.return_value = [sample_todo, Todo()]

    result = service.get_all_todos()

    assert len(result) == 2
    repository.find_all.assert_called_once()


def test_get_todo_by_id_when_exists_should_return_todo(service, repository, sample_todo):
    repository.find_by_id.return_value = sample_todo

    result = service.get_todo_by_id(1)

    assert result is not None
    assert result.title == "Test Todo"
    repository.find_by_id.assert_called_once_with(1)


def test_get_todo_by_id_when_not_exists_should_return_none(service, repository):
    repository.find_by_id.return_value = None

    result = service.get_todo_by_id(999)

    assert result is None
    repository.find_by_id.assert_called_once_with(999)


def test_get_todos_by_status_should_return_filtered_todos(service, repository, sample_todo):
    repository.find_by_completed.return_value = [sample_todo]

    result = service.get_todos_by_status(True)

    assert len(result) == 1
    repository.find_by_completed.assert_called_once_with(True)


def test_search_todos_should_return_matching_todos(service, repository, sample_todo):
    repository.find_by_title_containing_ignore_case.return_value = [sample_todo]

    result = service.search_todos("Test")

    assert len(result) == 1
    assert "Test" in result[0].title
    repository.find_by_title_containing_ignore_case.assert_called_once_with("Test")


def test_create_todo_should_save_todo(service, repository, sample_todo):
    repository.save.return_value = sample_todo

    result = service.create_todo(TodoCreate(title="Test Todo", description="Test Description"))

    assert result is not None
    assert result.title == "Test Todo"
    repository.save.assert_called_once()


def test_update_todo_when_exists_should_update_todo(service, repository, sample_todo):
    updated_details = TodoUpdate(title="Updated Title", description="Updated Description", completed=True)
    repository.find_by_id.return_value = sample_todo
    repository.save.return_value = sample_todo

    result = service.update_todo(1, updated_details)

    assert result is not None
    repository.find_by_id.assert_called_once_with(1)
    repository.save.assert_called_once()


def test_update_todo_when_not_exists_should_return_none(service, repository, sample_todo):
    repository.find_by_id.return_value = None

    result = service.update_todo(999, sample_todo)

    assert result is None
    repository.find_by_id.assert_called_once_with(999)
    repository.save.assert_not_called()


def test_delete_todo_when_exists_should_return_true(service, repository, sample_todo):
    repository.find_by_id.return_value = sample_todo

    result = service.delete_todo(1)

    assert result is True
    repository.find_by_id.assert_called_once_with(1)
    repository.delete.assert_called_once_with(sample_todo)


def test_delete_todo_when_not_exists_should_return_false(service, repository):
    repository.find_by_id.return_value = None

    result = service.delete_todo(999)

    assert result is False
    repository.find_by_id.assert_called_once_with(999)
    repository.delete.assert_not_called()


def test_toggle_todo_status_should_change_completed_status(service, repository, sample_todo):
    repository.find_by_id.return_value = sample_todo
    repository.save.return_value = sample_todo

    result = service.toggle_todo_status(1)

    assert result is not None
    repository.find_by_id.assert_called_once_with(1)
    repository.save.assert_called_once()
