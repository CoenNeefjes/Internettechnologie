package server;

import server.model.Client;
import server.model.Group;
import server.util.StringValidator;

public class test {

  public static void main(String[] args) {
    TestThread testThread = new TestThread();
    testThread.run();
  }
}
