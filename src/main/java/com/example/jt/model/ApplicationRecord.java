package com.example.jt.model;

import jakarta.validation.constraints.*;
import java.time.*;

public record ApplicationRecord(
  long id,
  @NotBlank String company,
  @NotBlank String role,
  @NotNull Status status,
  @NotNull LocalDate appliedOn,
  String notes,
  LocalDate lastUpdate
) {
  public boolean followUpDue() {
    LocalDate ref = lastUpdate != null ? lastUpdate : appliedOn;
    return ref != null && ref.plusDays(7).isBefore(LocalDate.now());
  }
}