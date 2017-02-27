package jp.uich;

public class ApplicationPropertyException extends RuntimeException {

  public ApplicationPropertyException(String key, String message, Throwable t) {
    super(createMessage(key, message), t);
  }

  public ApplicationPropertyException(String key, String message) {
    super(createMessage(key, message));
  }

  private static String createMessage(String key, String message) {
    return "ApplicationPropertyアクセス時にエラーが発生しました。 [key:[" + key + "], message:[" + message + "]]";
  }
}
