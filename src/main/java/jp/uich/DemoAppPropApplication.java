package jp.uich;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DemoAppPropApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoAppPropApplication.class, args);
  }
}
