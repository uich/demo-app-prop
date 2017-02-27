package jp.uich;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.uich.entity.ApplicationProperty;

//@Component
public class DefaultApplicationProperties implements ApplicationProperties {

  @Resource(name = "service.properties")
  private Properties properties;

  @Autowired
  private StringRedisTemplate redisOps;

  @Autowired
  private ApplicationPropertiesRepository repository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private HashOperations<String, String, String> hashOps() {
    return this.redisOps.opsForHash();
  }

  @Override
  public Integer getJoinableMinAge() {
    String value = this.hashOps().get(ApplicationProperties.class.getSimpleName(), "service.joinable.min.age");
    if (value == null) {
      throw new ApplicationPropertyException("service.joinable.min.age", "定義されていません。");
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      throw new ApplicationPropertyException("service.joinable.min.age", "数値ではありません。", e);
    }
  }

  @Override
  public String getUrl(Object object) {
    String urlFormat = (String) this.properties.get(object.getClass().getName() + ".url");
    if (StringUtils.isBlank(urlFormat)) {
      throw new ApplicationPropertyException(object.getClass().getName() + ".url", "定義されていません。");
    }
    try {
      Map<String, Object> placeholder = this.objectMapper.convertValue(object,
        new TypeReference<Map<String, Object>>() {});
      return StrSubstitutor.replace(urlFormat, placeholder, "{", "}");
    } catch (Exception e) {
      throw new ApplicationPropertyException(object.getClass().getName() + ".url", "URL文字列生成時にエラーが発生しました。", e);
    }
  }

  @Override
  @Transactional
  public LocalDate getServiceStartDate() {
    ApplicationProperty property = this.repository.getByKey("service.start.date");
    if (property == null || StringUtils.isBlank(property.getValue())) {
      throw new ApplicationPropertyException("service.start.date", "定義されていません。");
    }

    try {
      return LocalDate.parse(property.getValue(), DateTimeFormat.forPattern("yyyy-MM-dd"));
    } catch (IllegalArgumentException e) {
      throw new ApplicationPropertyException("service.start.date", "日付型のパースに失敗しました。", e);
    }
  }

}
