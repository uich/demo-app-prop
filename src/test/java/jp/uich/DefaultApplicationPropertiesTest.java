package jp.uich;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import jp.uich.entity.ApplicationProperty;
import jp.uich.entity.Item;
import jp.uich.entity.User;

@RunWith(SpringRunner.class)
@DataJpaTest
@SpringBootTest(classes = { RedisAutoConfiguration.class, PropertiesConfig.class })
public class DefaultApplicationPropertiesTest {

  @Configuration
  static class ContextConfiguration {

    @Bean
    public DefaultApplicationProperties applicationProperties() {
      return new DefaultApplicationProperties();
    }

  }

  @Autowired
  private DefaultApplicationProperties applicationProperties;

  @Autowired
  private StringRedisTemplate redisOps;

  @Autowired
  private TestEntityManager entityManager;

  @After
  public void tearDown() {
    this.redisOps.delete(ApplicationProperties.class.getSimpleName());
  }

  @Test
  public void testGetJoinableMinAge() {
    Integer age = new Random().nextInt(100);
    this.redisOps.opsForHash().put(ApplicationProperties.class.getSimpleName(), "service.joinable.min.age",
      String.valueOf(age));
    assertThat(this.applicationProperties.getJoinableMinAge()).isEqualTo(age);
  }

  @Test
  public void testGetUrl() {
    Item item = Item.builder()
      .id(new Random().nextLong())
      .name(RandomStringUtils.randomAlphanumeric(10))
      .build();
    assertThat(this.applicationProperties.getUrl(item)).isEqualTo("/items/" + item.getId());

    User user = User.builder()
      .id(new Random().nextLong())
      .name(RandomStringUtils.randomAlphanumeric(10))
      .build();
    assertThat(this.applicationProperties.getUrl(user)).isEqualTo("/users/" + user.getName());
  }

  @Test
  public void getServiceStartDate() {
    LocalDate serviceStartDate = LocalDate.parse("2017-02-27", DateTimeFormat.forPattern("yyyy-MM-dd"));
    this.entityManager.persist(new ApplicationProperty("service.start.date", serviceStartDate.toString("yyyy-MM-dd")));
    assertThat(this.applicationProperties.getServiceStartDate()).isEqualTo(serviceStartDate);
  }

}
