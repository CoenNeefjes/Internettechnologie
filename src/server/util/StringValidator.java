package server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidator {

  public static boolean validateNameString(String str) {
    Pattern pattern = Pattern.compile("\\W");
    Matcher matcher = pattern.matcher(str);
    return !matcher.find() && str.length() > 0;
  }

  public static boolean validateMessageString(String str) {
    return str.replaceAll(" ", "").length() > 0;
  }

}
