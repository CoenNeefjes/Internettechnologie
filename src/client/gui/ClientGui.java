package client.gui;

import client.ClientApplication;
import general.MsgType;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
  private JButton addGroupButton;

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
    addGroupButton.addActionListener(this::createGroup);

    updateClientList();
  }

  /* -------------- Variables -------------- */
  private MsgType msgType;
  private PrintWriter writer;
  private String userName;

  /* -------------- Getters -------------- */
  public String getReceipient() {
    return recipient.getText();
  }

  /* -------------- Setters -------------- */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setRecipient(String text, MsgType msgType) {
    this.msgType = msgType;
    recipient.setText(text);
  }

  public void updateClientList() {
    String result = "All\n";
    for (String client : ClientApplication.clientNames) {
      result += client + "\n";
    }
    clientList.setText(result);
  }

  public void updateGroupList() {
    String result = "";
    for (String group : ClientApplication.groupNames) {
      result += group + "\n";
    }
    groupList.setText(result);
  }

  public void receiveMessage(MsgType msgType, String groupName, String sender, String message) {
    if (sender.equals(userName)) {sender = "You";}
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + groupName + " " + sender + ": " + message + "\n");
  }

  public void receiveMessage(MsgType msgType, String sender, String message) {
    if (sender.equals(userName)) {sender = "You";}
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + sender + ": " + message + "\n");
  }

  /* -------------- ActionListeners -------------- */
  private void sendMessage(ActionEvent e) {
    String recipient = this.recipient.getText();
    if (recipient.equals("All")) {
      writer.println(MsgType.BCST + " " + textInput.getText());
    } else if (ClientApplication.clientNames.contains(recipient)) {

      writer.println(MsgType.PMSG + " " + recipient + " " + textInput.getText());
      chatBox.setText(
          chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
              + "You" + "to " + recipient + ": " + textInput.getText() + "\n");

    } else if (ClientApplication.groupNames.contains(recipient)) {
      writer.println(MsgType.GMSG + " " + recipient + " " + userName + " " + textInput.getText());
    } else {
      errorBox("Recipient is not in any list", "Error");
      return;
    }
    writer.flush();
    textInput.setText("");
  }

  private void createGroup(ActionEvent e) {
    writer.println(MsgType.CGRP + " " + createGroupBox());
    writer.flush();
  }

  /* -------------- UI Components -------------- */
  private void errorBox(String infoMessage, String titleBar) {
    JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar,
        JOptionPane.INFORMATION_MESSAGE);
  }

  private String createGroupBox() {
    return JOptionPane.showInputDialog("Enter group name: ");
  }

  /* -------------- Logic Methods -------------- */
  private boolean checkRecipient() {
    String recipient = this.recipient.getText();
    if (recipient.equals("All")) {
      msgType = MsgType.BCST;
    }
    if (ClientApplication.clientNames.contains(recipient)) {
      msgType = MsgType.PMSG;
    } else if (ClientApplication.groupNames.contains(recipient)) {
      msgType = MsgType.GMSG;
    } else {
      errorBox("Recipient is not in any list", "Error");
      return false;
    }
    return true;
  }
}
