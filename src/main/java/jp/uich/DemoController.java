package jp.uich;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jp.uich.entity.Item;
import jp.uich.entity.User;

@RestController
public class DemoController {

  @Autowired
  private ApplicationProperties properties;

  @GetMapping("/age")
  public Integer age() {
    return this.properties.getJoinableMinAge();
  }

  @GetMapping("/items/{id}/url")
  public String itemUrl(@PathVariable Long id) {
    return this.properties.getUrl(Item.builder().id(id).build());
  }

  @GetMapping("/users/{id}/url")
  public String userUrl(@PathVariable Long id) {
    return this.properties.getUrl(User.builder().id(id).name("kenny").build());
  }

  @GetMapping("/date")
  public LocalDate date() {
    return this.properties.getServiceStartDate();
  }
}
