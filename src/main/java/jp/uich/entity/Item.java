package jp.uich.entity;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item implements Entity {

  @Id
  private Long id;
  private String name;
}
