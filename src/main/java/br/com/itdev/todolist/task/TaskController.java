package br.com.itdev.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.itdev.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRespository taskRespository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody Task task, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início/termino deve ser maior do que a data atual");
        }
        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início deve ser anterior a data de termino");
        }

        var newTask = this.taskRespository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @GetMapping("/")
    public ResponseEntity<List<Task>> listUserTasks(HttpServletRequest request) {
        var idUser = (UUID) request.getAttribute("idUser");
        return ResponseEntity.status(HttpStatus.OK).body(this.taskRespository.findByIdUser(idUser));
    }

    @PutMapping("/{idOldTask}")
    public ResponseEntity<?> updateTask(@RequestBody Task updatedTask, @PathVariable UUID idOldTask,
            HttpServletRequest request) {
        var taskExists = this.taskRespository.findById(idOldTask).orElse(null);

        if (taskExists == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");

        if (!taskExists.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não tem permissão para alterar essa tarefa!");
        }

        Utils.copyNonNullProps(updatedTask, taskExists);

        return ResponseEntity.ok().body(this.taskRespository.save(taskExists));
    }
}
