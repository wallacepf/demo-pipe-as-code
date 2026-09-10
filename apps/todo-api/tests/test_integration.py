def test_should_create_and_retrieve_todo(client):
    create_response = client.post(
        "/api/todos",
        json={"title": "Integration Test Todo", "description": "Testing end-to-end flow"},
    )

    assert create_response.status_code == 201
    body = create_response.json()
    assert body["title"] == "Integration Test Todo"
    assert body["completed"] is False
    todo_id = body["id"]

    get_response = client.get(f"/api/todos/{todo_id}")

    assert get_response.status_code == 200
    assert get_response.json()["title"] == "Integration Test Todo"
    assert get_response.json()["description"] == "Testing end-to-end flow"


def test_should_update_todo(client):
    created = client.post("/api/todos", json={"title": "Original Title"}).json()

    response = client.put(
        f"/api/todos/{created['id']}",
        json={"title": "Updated Title", "description": "Updated Description", "completed": True},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["title"] == "Updated Title"
    assert body["completed"] is True


def test_should_toggle_todo_status(client):
    created = client.post("/api/todos", json={"title": "Toggle Test"}).json()
    assert created["completed"] is False
    todo_id = created["id"]

    first_toggle = client.patch(f"/api/todos/{todo_id}/toggle")
    assert first_toggle.status_code == 200
    assert first_toggle.json()["completed"] is True

    second_toggle = client.patch(f"/api/todos/{todo_id}/toggle")
    assert second_toggle.status_code == 200
    assert second_toggle.json()["completed"] is False


def test_should_delete_todo(client):
    created = client.post("/api/todos", json={"title": "To Be Deleted"}).json()
    todo_id = created["id"]

    delete_response = client.delete(f"/api/todos/{todo_id}")
    assert delete_response.status_code == 204

    get_response = client.get(f"/api/todos/{todo_id}")
    assert get_response.status_code == 404


def test_should_filter_todos_by_status(client):
    client.post("/api/todos", json={"title": "Completed Task", "completed": True})
    client.post("/api/todos", json={"title": "Pending Task", "completed": False})

    response = client.get("/api/todos", params={"completed": "true"})

    assert response.status_code == 200
    body = response.json()
    assert len(body) > 0
    assert all(item["completed"] is True for item in body)


def test_should_search_todos(client):
    client.post("/api/todos", json={"title": "Java Programming"})
    client.post("/api/todos", json={"title": "Python Learning"})

    response = client.get("/api/todos", params={"search": "Java"})

    assert response.status_code == 200
    body = response.json()
    assert len(body) > 0
    assert any("Java" in item["title"] for item in body)


def test_should_return_404_for_nonexistent_todo(client):
    response = client.get("/api/todos/99999")

    assert response.status_code == 404


def test_should_reject_invalid_todo(client):
    response = client.post("/api/todos", json={"title": ""})

    assert response.status_code == 422
