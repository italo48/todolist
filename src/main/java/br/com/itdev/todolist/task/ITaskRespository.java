package br.com.itdev.todolist.task;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITaskRespository extends JpaRepository<Task, UUID> {
    List<Task> findByIdUser(UUID idUser);
}
