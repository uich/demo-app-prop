package jp.uich;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface AbstractStored {

  @AliasFor("key")
  String value() default "";

  @AliasFor("value")
  String key() default "";
}
