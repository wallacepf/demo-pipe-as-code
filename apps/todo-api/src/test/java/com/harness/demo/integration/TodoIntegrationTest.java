package com.harness.demo.integration;

import com.harness.demo.TodoApplication;
import com.harness.demo.model.Todo;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TodoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TodoIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/todos";
    }

    @Test
    void shouldCreateAndRetrieveTodo() {
        Todo todo = new Todo();
        todo.setTitle("Integration Test Todo");
        todo.setDescription("Testing end-to-end flow");

        Integer todoId = given()
                .contentType(ContentType.JSON)
                .body(todo)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("title", equalTo("Integration Test Todo"))
                .body("completed", equalTo(false))
                .extract()
                .path("id");

        given()
                .when()
                .get("/" + todoId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Integration Test Todo"))
                .body("description", equalTo("Testing end-to-end flow"));
    }

    @Test
    void shouldUpdateTodo() {
        Todo todo = new Todo();
        todo.setTitle("Original Title");

        Integer todoId = given()
                .contentType(ContentType.JSON)
                .body(todo)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Todo updatedTodo = new Todo();
        updatedTodo.setTitle("Updated Title");
        updatedTodo.setDescription("Updated Description");
        updatedTodo.setCompleted(true);

        given()
                .contentType(ContentType.JSON)
                .body(updatedTodo)
                .when()
                .put("/" + todoId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"))
                .body("completed", equalTo(true));
    }

    @Test
    void shouldToggleTodoStatus() {
        Todo todo = new Todo();
        todo.setTitle("Toggle Test");

        Integer todoId = given()
                .contentType(ContentType.JSON)
                .body(todo)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("completed", equalTo(false))
                .extract()
                .path("id");

        given()
                .when()
                .patch("/" + todoId + "/toggle")
                .then()
                .statusCode(200)
                .body("completed", equalTo(true));

        given()
                .when()
                .patch("/" + todoId + "/toggle")
                .then()
                .statusCode(200)
                .body("completed", equalTo(false));
    }

    @Test
    void shouldDeleteTodo() {
        Todo todo = new Todo();
        todo.setTitle("To Be Deleted");

        Integer todoId = given()
                .contentType(ContentType.JSON)
                .body(todo)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .when()
                .delete("/" + todoId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/" + todoId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldFilterTodosByStatus() {
        Todo completedTodo = new Todo();
        completedTodo.setTitle("Completed Task");
        completedTodo.setCompleted(true);

        Todo pendingTodo = new Todo();
        pendingTodo.setTitle("Pending Task");
        pendingTodo.setCompleted(false);

        given().contentType(ContentType.JSON).body(completedTodo).post();
        given().contentType(ContentType.JSON).body(pendingTodo).post();

        given()
                .queryParam("completed", true)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("completed", everyItem(equalTo(true)));
    }

    @Test
    void shouldSearchTodos() {
        Todo todo1 = new Todo();
        todo1.setTitle("Java Programming");

        Todo todo2 = new Todo();
        todo2.setTitle("Python Learning");

        given().contentType(ContentType.JSON).body(todo1).post();
        given().contentType(ContentType.JSON).body(todo2).post();

        given()
                .queryParam("search", "Java")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("title", hasItem(containsString("Java")));
    }

    @Test
    void shouldReturn404ForNonExistentTodo() {
        given()
                .when()
                .get("/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldRejectInvalidTodo() {
        Todo invalidTodo = new Todo();
        invalidTodo.setTitle("");

        given()
                .contentType(ContentType.JSON)
                .body(invalidTodo)
                .when()
                .post()
                .then()
                .statusCode(400);
    }
}
