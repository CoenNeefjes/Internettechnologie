package server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidator {

  public static boolean validateString(String str) {
    Pattern pattern = Pattern.compile("\\W");
    Matcher matcher = pattern.matcher(str);
    return !matcher.find();
  }

}
