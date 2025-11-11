package com.example.jt.web;

import com.example.jt.model.*;
import com.example.jt.service.AppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/apps")
@Validated
public class ApiController {
  private final AppService service;

  public ApiController(AppService service) {
    this.service = service;
  }

  @GetMapping
  public List<ApplicationRecord> list(@RequestParam(name = "q", required = false) String q,
                                      @RequestParam(name = "status", required = false) Status status) {
    return service.list(q, status);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApplicationRecord> get(@PathVariable("id") long id) {
    ApplicationRecord rec = service.get(id);
    return rec != null ? ResponseEntity.ok(rec) : ResponseEntity.notFound().build();
  }

  public record CreateReq(@NotBlank String company,
                          @NotBlank String role,
                          @NotNull Status status,
                          @NotNull LocalDate appliedOn,
                          String notes) {}

  @PostMapping
  public ResponseEntity<ApplicationRecord> create(@Valid @RequestBody CreateReq req) {
    ApplicationRecord rec = service.create(req.company(), req.role(), req.status(), req.appliedOn(), req.notes());
    return ResponseEntity.ok(rec);
  }

  public record UpdateReq(@NotBlank String company,
                          @NotBlank String role,
                          @NotNull Status status,
                          @NotNull LocalDate appliedOn,
                          String notes,
                          LocalDate lastUpdate) {}

  @PutMapping("/{id}")
  public ResponseEntity<ApplicationRecord> update(@PathVariable("id") long id, @Valid @RequestBody UpdateReq req) {
    ApplicationRecord rec = service.update(id, req.company(), req.role(), req.status(), req.appliedOn(), req.notes(), req.lastUpdate());
    return rec != null ? ResponseEntity.ok(rec) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> exportCsv() {
    byte[] data = service.exportCsv();
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=applications.csv")
      .contentType(MediaType.TEXT_PLAIN)
      .body(data);
  }

  @PostMapping("/import")
  public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
    int imported = service.importCsv(file.getInputStream());
    return ResponseEntity.ok("Imported records: " + imported);
  }
}