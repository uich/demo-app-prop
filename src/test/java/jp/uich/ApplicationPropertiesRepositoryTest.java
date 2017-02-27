package jp.uich;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import jp.uich.entity.ApplicationProperty;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ApplicationPropertiesRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ApplicationPropertiesRepository repository;

  @Test
  @Transactional
  public void test() {
    ApplicationProperty entity = new ApplicationProperty("foo", "bar");
    this.entityManager.persist(entity);
    assertThat(this.repository.getByKey("foo")).isEqualTo(entity);
  }

}
