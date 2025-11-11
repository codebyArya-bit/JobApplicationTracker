package com.example.jt.web;

import com.example.jt.model.ApplicationRecord;
import com.example.jt.model.Status;
import com.example.jt.service.AppService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@Validated
public class AppController {
  private final AppService service;

  public AppController(AppService service) {
    this.service = service;
  }

  public record AppForm(@NotBlank String company,
                        @NotBlank String role,
                        @NotNull Status status,
                        @NotNull LocalDate appliedOn,
                        String notes,
                        LocalDate lastUpdate) {}

  @GetMapping("/")
  public @ResponseBody String index(@RequestParam(name = "q", required = false) String q,
                      @RequestParam(name = "status", required = false) String statusStr,
                      Model model) {
    Status status = null;
    if (statusStr != null && !statusStr.isBlank()) {
      try { status = Status.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}
    }
    model.addAttribute("records", service.list(q, status));
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("status", status);
    model.addAttribute("statuses", Status.values());
    model.addAttribute("form", new AppForm("", "", Status.APPLIED, LocalDate.now(), "", LocalDate.now()));
    return "OK";
  }

  @GetMapping("/test")
  public String test(Model model) {
    model.addAttribute("statuses", Status.values());
    model.addAttribute("status", null);
    return "test";
  }

  @PostMapping("/create")
  public String create(@ModelAttribute AppForm form) {
    service.create(form.company(), form.role(), form.status(), form.appliedOn(), form.notes());
    return "redirect:/";
  }

  @GetMapping("/edit/{id}")
  public String edit(@PathVariable("id") long id, Model model) {
    ApplicationRecord rec = service.get(id);
    if (rec == null) return "redirect:/";
    AppForm form = new AppForm(rec.company(), rec.role(), rec.status(), rec.appliedOn(), rec.notes(), rec.lastUpdate());
    model.addAttribute("record", rec);
    model.addAttribute("form", form);
    model.addAttribute("statuses", Status.values());
    return "home"; // reuse page with form section
  }

  @PostMapping("/update/{id}")
  public String update(@PathVariable("id") long id, @ModelAttribute AppForm form) {
    service.update(id, form.company(), form.role(), form.status(), form.appliedOn(), form.notes(), form.lastUpdate());
    return "redirect:/";
  }

  @PostMapping("/delete/{id}")
  public String delete(@PathVariable("id") long id) {
    service.delete(id);
    return "redirect:/";
  }

  @GetMapping("/export")
  public @ResponseBody byte[] export() {
    return service.exportCsv();
  }

  @PostMapping("/import")
  public String importCsv(@RequestParam("file") MultipartFile file) throws IOException {
    service.importCsv(file.getInputStream());
    return "redirect:/";
  }
}