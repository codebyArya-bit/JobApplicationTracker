package com.example.jt.service;

import com.example.jt.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AppService {
  private final Map<Long, ApplicationRecord> store = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(1);

  public List<ApplicationRecord> list(String q, Status status) {
    return store.values().stream()
      .filter(a -> q == null || q.isBlank() ||
        safeContains(a.company(), q) ||
        safeContains(a.role(), q) ||
        safeContains(a.notes(), q))
      .filter(a -> status == null || a.status() == status)
      .sorted(Comparator.comparing(ApplicationRecord::appliedOn).reversed())
      .collect(Collectors.toList());
  }

  public ApplicationRecord get(long id) {
    return store.get(id);
  }

  public ApplicationRecord create(String company, String role, Status status, LocalDate appliedOn, String notes) {
    long id = seq.getAndIncrement();
    ApplicationRecord rec = new ApplicationRecord(id, company, role, status, appliedOn, notes, appliedOn);
    store.put(id, rec);
    return rec;
  }

  public ApplicationRecord update(long id, String company, String role, Status status, LocalDate appliedOn, String notes, LocalDate lastUpdate) {
    ApplicationRecord existing = store.get(id);
    if (existing == null) return null;
    LocalDate finalLastUpdate = lastUpdate != null ? lastUpdate : existing.lastUpdate();
    ApplicationRecord rec = new ApplicationRecord(id, company, role, status, appliedOn, notes, finalLastUpdate);
    store.put(id, rec);
    return rec;
  }

  public boolean delete(long id) {
    return store.remove(id) != null;
  }

  public byte[] exportCsv() {
    StringBuilder sb = new StringBuilder();
    sb.append("id,company,role,status,appliedOn,notes,lastUpdate\n");
    for (ApplicationRecord a : store.values().stream()
            .sorted(Comparator.comparing(ApplicationRecord::id))
            .toList()) {
      sb.append(a.id()).append(',')
        .append(csvEscape(a.company())).append(',')
        .append(csvEscape(a.role())).append(',')
        .append(a.status()).append(',')
        .append(a.appliedOn()).append(',')
        .append(csvEscape(a.notes())).append(',')
        .append(a.lastUpdate() != null ? a.lastUpdate() : "")
        .append('\n');
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public int importCsv(InputStream in) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      boolean first = true;
      int count = 0;
      while ((line = br.readLine()) != null) {
        if (first) { first = false; continue; }
        List<String> cols = parseCsvLine(line);
        if (cols.size() < 7) continue;
        // id,company,role,status,appliedOn,notes,lastUpdate
        String company = unquote(cols.get(1));
        String role = unquote(cols.get(2));
        Status status = Status.valueOf(cols.get(3));
        LocalDate appliedOn = LocalDate.parse(cols.get(4));
        String notes = unquote(cols.get(5));
        LocalDate lastUpdate = cols.get(6) != null && !cols.get(6).isBlank() ? LocalDate.parse(cols.get(6)) : appliedOn;
        ApplicationRecord rec = create(company, role, status, appliedOn, notes);
        // Maintain original lastUpdate if provided
        update(rec.id(), rec.company(), rec.role(), rec.status(), rec.appliedOn(), rec.notes(), lastUpdate);
        count++;
      }
      return count;
    }
  }

  private static String csvEscape(String s) {
    if (s == null) return "";
    boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"");
    String escaped = s.replace("\"", "\"\"");
    return needsQuotes ? "\"" + escaped + "\"" : escaped;
  }

  private static String unquote(String s) {
    if (s == null) return null;
    String t = s.trim();
    if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
      t = t.substring(1, t.length() - 1).replace("\"\"", "\"");
    }
    return t;
  }

  private static List<String> parseCsvLine(String line) {
    List<String> cols = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (inQuotes) {
        if (c == '"') {
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            cur.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          cur.append(c);
        }
      } else {
        if (c == ',') {
          cols.add(cur.toString());
          cur.setLength(0);
        } else if (c == '"') {
          inQuotes = true;
        } else {
          cur.append(c);
        }
      }
    }
    cols.add(cur.toString());
    // Re-wrap quoted values to match unquote expectations
    return cols.stream().map(v -> v.startsWith("\"") || v.contains(",") ? "\"" + v + "\"" : v).collect(Collectors.toList());
  }

  private static boolean safeContains(String hay, String needle) {
    if (hay == null || needle == null) return false;
    return hay.toLowerCase().contains(needle.toLowerCase());
  }
}