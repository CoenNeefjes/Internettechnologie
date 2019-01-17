package server.util;

public class ErrorMessageConstructor {

  public static String clientNotFoundError() {
    return "-ERR client not found";
  }

  public static String alreadyLoggedInError() {
    return "-ERR user already logged in";
  }

  public static String groupNameAlreadyExistsError() {
    return "-ERR group name already exists";
  }

  public static String groupNotFoundError() {
    return "-ERR group not found";
  }

  public static String clientNotInGroupError() {
    return "-ERR user is not in this group";
  }

  public static String invalidNameError(String name) { return "-ERR " + name + " is not a valid name"; }

  public static String notOwnerOfGroupError() {
    return "-ERR you are not the owner of the group";
  }

  public static String clientAlreadyInGroupError() { return "-ERR you are already in this group"; }

}
