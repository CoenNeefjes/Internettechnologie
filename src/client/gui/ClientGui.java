package client.gui;

import client.ClientApplication;
import general.MsgType;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class ClientGui extends JFrame {

  private JPanel rootPanel;
  private JTextArea textInput;
  private JButton sendButton;
  private JTextPane groupList;
  private JTextPane clientList;
  private JTextPane chatBox;
  private JPanel clientsPanel;
  private JPanel groupsPanel;
  private JPanel textInputPanel;
  private JPanel chatBoxPanel;
  private JTextArea recipient;

  public ClientGui(PrintWriter writer) {
    this.writer = writer;

    add(rootPanel);

    setTitle("ClientGui");
    setSize(600, 400);

//    recipient.setEditable(false);
    clientList.setEditable(false);
    groupList.setEditable(false);
    chatBox.setEditable(false);
    sendButton.addActionListener(this::sendMessage);

    updateClientList();
  }

  private MsgType msgType;
  private PrintWriter writer;

  public void setRecipient(String text, MsgType msgType) {
    this.msgType = msgType;
    recipient.setText(text);
  }

  public String getReceipient() {
    return recipient.getText();
  }

  public void sendMessage(ActionEvent e) {
    checkRecipient();
    writer.println(this.msgType + " " + textInput.getText());
    writer.flush();
    receiveMessage(this.msgType, "You", textInput.getText());
    textInput.setText("");
  }

  public void receiveMessage(MsgType msgType, String sender, String message) {
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + sender + ": " + message + "\n");
  }

  // This one is for the group messages
  public void receiveMessage(MsgType msgType, String groupName, String sender, String message) {
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + groupName + " " + sender + ": " + message + "\n");
  }

  public void updateClientList() {
    String result = "All\n";
    for (String client: ClientApplication.clientNames) {
      result += client + "\n";
    }
    clientList.setText(result);
  }

  public void updateGroupList() {
    String result = "";
    for (String group: ClientApplication.groupNames) {
      result += group + "\n";
    }
    clientList.setText(result);
  }

  private void checkRecipient() {
    String recipient = this.recipient.getText();
    if (recipient.equals("All")) {
      msgType = MsgType.BCST;
    }
  }
}
