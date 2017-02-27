package jp.uich;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AbstractStored
public @interface StoredFile {

  @AliasFor(annotation = AbstractStored.class)
  String value() default "";

  @AliasFor(annotation = AbstractStored.class)
  String key() default "";

}
