package jp.uich;

import org.joda.time.LocalDate;

public interface ApplicationProperties {

  @StoredRedis("service.joinable.min.age")
  Integer getJoinableMinAge();

  @StoredFile("#object.class.name + '.url'")
  String getUrl(Object object);

  @StoredDB("service.start.date")
  LocalDate getServiceStartDate();
}
