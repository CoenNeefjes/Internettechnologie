package server.util;

/**
 * Class with only static Strings representing all the error messages the server can return
 *
 * @author Coen Neefjes
 */
public class ErrorMessageConstructor {

  public static String clientNotFoundError() {
    return "-ERR Client not found";
  }

  public static String alreadyLoggedInError() {
    return "-ERR User already logged in";
  }

  public static String groupNameAlreadyExistsError() {
    return "-ERR Group name already exists";
  }

  public static String groupNotFoundError() {
    return "-ERR Group not found";
  }

  public static String clientNotInGroupError() {
    return "-ERR User is not in this group";
  }

  public static String invalidNameError(String name) { return "-ERR " + name + " is not a valid name"; }

  public static String notOwnerOfGroupError() {
    return "-ERR You are not the owner of the group";
  }

  public static String clientAlreadyInGroupError() { return "-ERR You are already in this group"; }

  public static String invalidInputError() { return "-ERR Invalid input"; }

  public static String sendToSelfError() { return "-ERR You cannot send to yourself"; }

  public static String kickSelfError() { return "-ERR You cannot kick yourself"; }

  public static String emptyMessageError() { return "-ERR Empty messages are not allowed"; }

}
