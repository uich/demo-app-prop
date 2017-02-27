package jp.uich.entity;

import javax.persistence.Column;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User implements Entity {

  private Long id;
  @Column(unique = true)
  private String name;
}
