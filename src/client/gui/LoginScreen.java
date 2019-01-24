package client.gui;

import client.service.MessageProcessor;
import general.MsgType;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * GUI class for the login screen
 *
 * @author Coen Neefjes
 */
public class LoginScreen extends JFrame {

  private JTextField textField1;
  private JPanel panel1;
  private JButton loginButton;

  private MessageProcessor messageProcessor;

  public LoginScreen(MessageProcessor messageProcessor) {
    this.messageProcessor = messageProcessor;
    add(panel1);

    setTitle("ClientGui");
    setSize(300, 200);

    this.setLocationRelativeTo(null);

    loginButton.addActionListener(this::sendMessage);

    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public void errorBox(String infoMessage) {
    JOptionPane.showMessageDialog(null, infoMessage, "Error",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void sendMessage(ActionEvent e) {
    String userName = textField1.getText();
    messageProcessor.sendMessage(MsgType.HELO + " " + userName);
  }
}
