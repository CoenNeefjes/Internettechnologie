package client.gui;

import client.LoggedInCallBack;
import client.service.MessageProcessor;
import general.MsgType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginScreen extends JFrame {

  private JTextField textField1;
  private JPanel panel1;
  private JButton loginButton;

  private MessageProcessor messageProcessor;

  private LoggedInCallBack startClientGuiCallBack;

  public LoginScreen(MessageProcessor messageProcessor, LoggedInCallBack callBack) {
    this.startClientGuiCallBack = callBack;
    this.messageProcessor = messageProcessor;
    add(panel1);

    setTitle("ClientGui");
    setSize(300, 200);

    this.setLocationRelativeTo(null);

    loginButton.addActionListener(this::sendMessage);
  }

  private void sendMessage(ActionEvent e) {
    String userName = textField1.getText();
    messageProcessor.sendMessage(MsgType.HELO + " " + userName);
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    startClientGuiCallBack.startClientGui(userName);
  }
}
