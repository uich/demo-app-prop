package jp.uich;

import org.springframework.data.repository.Repository;

import jp.uich.entity.ApplicationProperty;

public interface ApplicationPropertiesRepository extends Repository<ApplicationProperty, String> {

  ApplicationProperty getByKey(String key);
}
