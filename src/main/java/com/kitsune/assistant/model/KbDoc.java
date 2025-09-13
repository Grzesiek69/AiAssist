package com.kitsune.assistant.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="t_kb_docs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KbDoc {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  String source;   // faq/policy/howto/spec
  String title;
  @Column(columnDefinition="text") String body;
  String lang;     // 'pl','en'
}
