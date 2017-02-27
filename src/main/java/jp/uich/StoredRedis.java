package jp.uich;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AbstractStored
public @interface StoredRedis {

  /**
   * @return Redisで管理するキー文字列(SpELの指定も可能です)
   */
  @AliasFor(annotation = AbstractStored.class)
  String value() default "";

  @AliasFor(annotation = AbstractStored.class)
  String key() default "";

  /**
   * @return プロパティのデフォルト値。存在しなければアプリケーション起動時にこの値で初期化します。
   * @see ApplicationPropertyManager
   */
  String defaultValue() default "";

}
