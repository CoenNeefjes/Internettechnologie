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

  public static String userNotInGroupError() {
    return "-ERR user is not in this group";
  }

  public static String notOwnerOfGroupError() {
    return "-ERR you are not the owner of the group";
  }

}
