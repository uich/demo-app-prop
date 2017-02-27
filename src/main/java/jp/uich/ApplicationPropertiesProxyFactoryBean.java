package jp.uich;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.uich.entity.ApplicationProperty;
import lombok.Getter;
import lombok.NonNull;

@Component
public class ApplicationPropertiesProxyFactoryBean implements FactoryBean<ApplicationProperties>, InitializingBean {

  /** メソッド引数の名前を解決する */
  private static final ParameterNameDiscoverer PARAM_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

  /** Proxyオブジェクト */
  @Getter
  private ApplicationProperties object;

  /** プロパティの文字列からメソッドの戻りの型に変換するためのサービス */
  private FormattingConversionService conversionService = new DefaultFormattingConversionService();
  /** キー文字列で指定され得るSpEL式をパースする */
  private ExpressionParser elParser = new SpelExpressionParser();

  /** ファイルに定義されたプロパティ全部 */
  @Resource(name = "service.properties")
  private Properties properties;

  /** Redisに突っ込んだプロパティにアクセスする */
  @Autowired
  private StringRedisTemplate redisOps;
  /** DBに突っ込んだプロパティにアクセスする */
  @Autowired
  private ApplicationPropertiesRepository repository;

  /** プロパティ値に埋め込まれたプレースホルダを生成する */
  private ObjectMapper objectMapper = new ObjectMapper();

  private HashOperations<String, String, String> hashOps() {
    return this.redisOps.opsForHash();
  }

  @Override
  public void afterPropertiesSet() {
    ConversionServiceFactory.registerConverters(Collections.singleton(StringToLocalDateConverter.INSTANCE),
      this.conversionService);

    this.object = (ApplicationProperties) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
      new Class[] { ApplicationProperties.class }, (proxy, method, args) -> {
        // equals&toSring&hashCodeの実装
        if (ReflectionUtils.isEqualsMethod(method)) {
          return EqualsBuilder.reflectionEquals(this.object, args[0]);
        }
        if (ReflectionUtils.isToStringMethod(method)) {
          return ToStringBuilder.reflectionToString(this.object);
        }
        if (ReflectionUtils.isHashCodeMethod(method)) {
          return HashCodeBuilder.reflectionHashCode(this.object);
        }

        // キー文字列を取得する
        String key = this.getKey(proxy, method, args);
        // 純粋に定義されている値をそのまま文字列型で取得する
        String valueAsText = this.getPureValue(key, method);

        // 今回は検証用なので引数一つの場合のみ対応
        if (args != null && args.length == 1) {
          try {
            // 指定された引数からプレースホルダ生成
            Map<String, Object> placeholder = this.objectMapper.convertValue(args[0],
              new TypeReference<Map<String, Object>>() {});
            // プロパティ値のプレースホルダに値を埋め込む
            valueAsText = StrSubstitutor.replace(valueAsText, placeholder, "{", "}");
          } catch (Exception e) {
            throw new ApplicationPropertyException(key, "プレースホルダバインド時にエラーが発生しました。]", e);
          }
        }

        // メソッドの戻りの型
        TypeDescriptor toType = new TypeDescriptor(MethodParameter.forMethodOrConstructor(method, -1));

        try {
          // メソッドの戻りの型に変換
          return this.conversionService.convert(valueAsText, toType);
        } catch (Exception e) {
          throw new ApplicationPropertyException(key, "戻り値の変換処理に失敗しました。", e);
        }
      });
  }

  @NonNull
  private String getPureValue(String key, Method method) {
    // DBに格納されている値を取得する
    StoredDB storedDB = AnnotatedElementUtils.findMergedAnnotation(method, StoredDB.class);
    if (storedDB != null) {
      Assert.isTrue(StringUtils.isNotBlank(key), "キーが空文字列です。 [method:[" + method + "]]");
      return Optional.ofNullable(this.repository.getByKey(key))
        .map(ApplicationProperty::getValue)
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new ApplicationPropertyException(storedDB.value(), "定義されていません。"));
    }
    // Redisに格納されている値を取得する
    StoredRedis storedRedis = AnnotatedElementUtils.findMergedAnnotation(method, StoredRedis.class);
    if (storedRedis != null) {
      Assert.isTrue(StringUtils.isNotBlank(key), "キーが空文字列です。 [method:[" + method + "]]");

      return Optional.ofNullable(this.hashOps().get(ApplicationProperties.class.getSimpleName(), key))
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new ApplicationPropertyException(key, "定義されていません。"));
    }

    // プロパティファイルに格納されている値を取得する
    StoredFile storedFile = AnnotatedElementUtils.findMergedAnnotation(method, StoredFile.class);
    if (storedFile != null) {
      Assert.isTrue(StringUtils.isNotBlank(key), "キーが空文字列です。 [method:[" + method + "]]");

      return Optional.ofNullable((String) this.properties.get(key))
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new ApplicationPropertyException(key, "定義されていません。"));
    }

    throw new IllegalStateException("データの定義先が指定されていません。 [method:[" + method + "]]");
  }

  private String getKey(Object proxy, Method method, Object[] args) {
    StandardEvaluationContext evalContext = this.createEvalContext(proxy, method, args);
    AbstractStored stored = AnnotatedElementUtils.findMergedAnnotation(method, AbstractStored.class);
    String key = stored.key();
    Assert.isTrue(StringUtils.isNotBlank(key), "キーが空文字列です。 [method:[" + method + "]]");

    try {
      return this.elParser.parseExpression(key).getValue(evalContext, String.class);
    } catch (Exception e) {
      return key;
    }
  }

  private StandardEvaluationContext createEvalContext(Object proxy, Method method, Object[] args) {
    StandardEvaluationContext evalContext = new StandardEvaluationContext();
    evalContext.setRootObject(proxy);
    if (ArrayUtils.isEmpty(args)) {
      return evalContext;
    }
    IntStream.range(0, args.length)
      .forEach(n -> {
        MethodParameter param = MethodParameter.forMethodOrConstructor(method, n);
        param.initParameterNameDiscovery(PARAM_NAME_DISCOVERER);
        String name = param.getParameterName();
        Object value = args[n];
        evalContext.setVariable(name, value);
        evalContext.setVariable("a" + n, value);
      });

    return evalContext;
  }

  @Override
  public Class<?> getObjectType() {
    return ApplicationProperties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  private enum StringToLocalDateConverter implements Converter<String, LocalDate> {
    INSTANCE;

    @Override
    public LocalDate convert(String source) {
      return LocalDate.parse(source, DateTimeFormat.forPattern("yyyy-MM-dd"));
    }
  }
}
