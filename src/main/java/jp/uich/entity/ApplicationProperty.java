package jp.uich.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperty {

  @Id
  @Column(name = "`key`")
  private String key;
  @Column(nullable = false)
  private String value;
}
