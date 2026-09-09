package com.harness.demo.service;

import com.harness.demo.model.Todo;
import com.harness.demo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
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
    void getAllTodos_ShouldReturnAllTodos() {
        List<Todo> todos = Arrays.asList(sampleTodo, new Todo());
        when(todoRepository.findAll()).thenReturn(todos);

        List<Todo> result = todoService.getAllTodos();

        assertThat(result).hasSize(2);
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    void getTodoById_WhenExists_ShouldReturnTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));

        Optional<Todo> result = todoService.getTodoById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Todo");
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    void getTodoById_WhenNotExists_ShouldReturnEmpty() {
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Todo> result = todoService.getTodoById(999L);

        assertThat(result).isEmpty();
        verify(todoRepository, times(1)).findById(999L);
    }

    @Test
    void getTodosByStatus_ShouldReturnFilteredTodos() {
        List<Todo> completedTodos = Arrays.asList(sampleTodo);
        when(todoRepository.findByCompleted(true)).thenReturn(completedTodos);

        List<Todo> result = todoService.getTodosByStatus(true);

        assertThat(result).hasSize(1);
        verify(todoRepository, times(1)).findByCompleted(true);
    }

    @Test
    void searchTodos_ShouldReturnMatchingTodos() {
        List<Todo> matchingTodos = Arrays.asList(sampleTodo);
        when(todoRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingTodos);

        List<Todo> result = todoService.searchTodos("Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Test");
        verify(todoRepository, times(1)).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void createTodo_ShouldSaveTodo() {
        when(todoRepository.save(any(Todo.class))).thenReturn(sampleTodo);

        Todo result = todoService.createTodo(sampleTodo);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void updateTodo_WhenExists_ShouldUpdateTodo() {
        Todo updatedDetails = new Todo();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setCompleted(true);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(sampleTodo);

        Optional<Todo> result = todoService.updateTodo(1L, updatedDetails);

        assertThat(result).isPresent();
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void updateTodo_WhenNotExists_ShouldReturnEmpty() {
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Todo> result = todoService.updateTodo(999L, sampleTodo);

        assertThat(result).isEmpty();
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void deleteTodo_WhenExists_ShouldReturnTrue() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
        doNothing().when(todoRepository).delete(any(Todo.class));

        boolean result = todoService.deleteTodo(1L);

        assertThat(result).isTrue();
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).delete(any(Todo.class));
    }

    @Test
    void deleteTodo_WhenNotExists_ShouldReturnFalse() {
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        boolean result = todoService.deleteTodo(999L);

        assertThat(result).isFalse();
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    void toggleTodoStatus_ShouldChangeCompletedStatus() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(sampleTodo);

        Optional<Todo> result = todoService.toggleTodoStatus(1L);

        assertThat(result).isPresent();
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }
}
