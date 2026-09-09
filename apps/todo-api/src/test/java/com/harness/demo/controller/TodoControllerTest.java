package com.harness.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.demo.model.Todo;
import com.harness.demo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private Todo sampleTodo;

    @BeforeEach
    void setUp() {
        sampleTodo = new Todo();
        sampleTodo.setId(1L);
        sampleTodo.setTitle("Test Todo");
        sampleTodo.setDescription("Test Description");
        sampleTodo.setCompleted(false);
    }

    @Test
    void getAllTodos_ShouldReturnTodoList() throws Exception {
        when(todoService.getAllTodos()).thenReturn(Arrays.asList(sampleTodo));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo")));

        verify(todoService, times(1)).getAllTodos();
    }

    @Test
    void getAllTodos_WithCompletedFilter_ShouldReturnFilteredList() throws Exception {
        when(todoService.getTodosByStatus(true)).thenReturn(Arrays.asList(sampleTodo));

        mockMvc.perform(get("/api/todos?completed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(todoService, times(1)).getTodosByStatus(true);
    }

    @Test
    void getAllTodos_WithSearchFilter_ShouldReturnSearchResults() throws Exception {
        when(todoService.searchTodos("Test")).thenReturn(Arrays.asList(sampleTodo));

        mockMvc.perform(get("/api/todos?search=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(todoService, times(1)).searchTodos("Test");
    }

    @Test
    void getTodoById_WhenExists_ShouldReturnTodo() throws Exception {
        when(todoService.getTodoById(1L)).thenReturn(Optional.of(sampleTodo));

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Todo")))
                .andExpect(jsonPath("$.description", is("Test Description")));

        verify(todoService, times(1)).getTodoById(1L);
    }

    @Test
    void getTodoById_WhenNotExists_ShouldReturn404() throws Exception {
        when(todoService.getTodoById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).getTodoById(999L);
    }

    @Test
    void createTodo_WithValidData_ShouldReturnCreated() throws Exception {
        when(todoService.createTodo(any(Todo.class))).thenReturn(sampleTodo);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTodo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Todo")));

        verify(todoService, times(1)).createTodo(any(Todo.class));
    }

    @Test
    void createTodo_WithInvalidData_ShouldReturn400() throws Exception {
        Todo invalidTodo = new Todo();
        invalidTodo.setTitle("");

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTodo)))
                .andExpect(status().isBadRequest());

        verify(todoService, never()).createTodo(any(Todo.class));
    }

    @Test
    void updateTodo_WhenExists_ShouldReturnUpdatedTodo() throws Exception {
        when(todoService.updateTodo(anyLong(), any(Todo.class))).thenReturn(Optional.of(sampleTodo));

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Todo")));

        verify(todoService, times(1)).updateTodo(anyLong(), any(Todo.class));
    }

    @Test
    void updateTodo_WhenNotExists_ShouldReturn404() throws Exception {
        when(todoService.updateTodo(anyLong(), any(Todo.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTodo)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).updateTodo(anyLong(), any(Todo.class));
    }

    @Test
    void toggleTodoStatus_WhenExists_ShouldReturnUpdatedTodo() throws Exception {
        sampleTodo.setCompleted(true);
        when(todoService.toggleTodoStatus(1L)).thenReturn(Optional.of(sampleTodo));

        mockMvc.perform(patch("/api/todos/1/toggle"))
                .andExpect(status().isOk());

        verify(todoService, times(1)).toggleTodoStatus(1L);
    }

    @Test
    void deleteTodo_WhenExists_ShouldReturn204() throws Exception {
        when(todoService.deleteTodo(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo(1L);
    }

    @Test
    void deleteTodo_WhenNotExists_ShouldReturn404() throws Exception {
        when(todoService.deleteTodo(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).deleteTodo(999L);
    }
}
