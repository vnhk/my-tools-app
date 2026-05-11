package com.bervan.toolsapp.views.asynctask;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.AsyncTaskService;
import com.bervan.asynctask.HistoryAsyncTask;
import com.bervan.asynctask.HistoryAsyncTaskService;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.search.model.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/async/async-tasks")
public class AsyncTaskRestController {

    private final AsyncTaskService asyncTaskService;
    private final HistoryAsyncTaskService historyAsyncTaskService;

    public AsyncTaskRestController(AsyncTaskService asyncTaskService, HistoryAsyncTaskService historyAsyncTaskService) {
        this.asyncTaskService = asyncTaskService;
        this.historyAsyncTaskService = historyAsyncTaskService;
    }

    record AsyncTaskDto(String id, String status, String message,
                        LocalDateTime creationDate, LocalDateTime modificationDate,
                        LocalDateTime startDate, LocalDateTime endDate,
                        boolean notified, boolean notifyOnSuccess,
                        int timeoutInMin, boolean deleted) {}

    record AsyncTaskHistoryDto(String id, String asyncTaskId,
                               LocalDateTime modificationDate,
                               String status, String message) {}

    @GetMapping
    public ResponseEntity<Page<AsyncTaskDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "modificationDate") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        SortDirection sortDir = "asc".equalsIgnoreCase(direction) ? SortDirection.ASC : SortDirection.DESC;
        List<AsyncTask> all = asyncTaskService.load(new SearchRequest(), PageRequest.of(0, 100_000), sort, sortDir);
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<AsyncTaskDto> dtos = all.subList(from, to).stream().map(this::toDto).toList();
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page, size), total));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsyncTaskDto> getById(@PathVariable String id) {
        AsyncTask task = asyncTaskService.findById(UUID.fromString(id));
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDto(task));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<Page<AsyncTaskHistoryDto>> getHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "modificationDate") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        SortDirection sortDir = "asc".equalsIgnoreCase(direction) ? SortDirection.ASC : SortDirection.DESC;
        SearchRequest request = new SearchRequest();
        request.addCriterion("HISTORY_OWNER_CRITERIA", HistoryAsyncTask.class,
                "asyncTask.id", SearchOperation.EQUALS_OPERATION, id);
        List<HistoryAsyncTask> all = historyAsyncTaskService.load(request, PageRequest.of(0, 100_000), sort, sortDir);
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<AsyncTaskHistoryDto> dtos = all.subList(from, to).stream().map(this::toHistoryDto).toList();
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page, size), total));
    }

    private AsyncTaskDto toDto(AsyncTask t) {
        return new AsyncTaskDto(
                t.getId().toString(), t.getStatus(), t.getMessage(),
                t.getCreationDate(), t.getModificationDate(),
                t.getStartDate(), t.getEndDate(),
                Boolean.TRUE.equals(t.getNotified()),
                Boolean.TRUE.equals(t.getNotifyOnSuccess()),
                t.getTimeoutInMin() != null ? t.getTimeoutInMin() : 60,
                Boolean.TRUE.equals(t.isDeleted())
        );
    }

    private AsyncTaskHistoryDto toHistoryDto(HistoryAsyncTask h) {
        return new AsyncTaskHistoryDto(
                h.getId().toString(),
                h.getAsyncTask() != null ? h.getAsyncTask().getId().toString() : null,
                h.getModificationDate(),
                h.getStatus(),
                h.getMessage()
        );
    }
}
