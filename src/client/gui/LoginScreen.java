package client.gui;

import client.LoggedInCallBack;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginScreen extends JFrame {

  private JTextField textField1;
  private JPanel panel1;
  private JButton loginButton;

  private PrintWriter writer;

  private LoggedInCallBack startClientGuiCallBack;

  public LoginScreen(PrintWriter writer, LoggedInCallBack callBack) {
    this.startClientGuiCallBack = callBack;
    this.writer = writer;
    add(panel1);

    setTitle("ClientGui");
    setSize(600, 400);

    loginButton.addActionListener(this::sendMessage);
  }

  private void sendMessage(ActionEvent e) {
    writer.println("HELO " + textField1.getText());
    writer.flush();
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    startClientGuiCallBack.startClientGui();
  }
}
