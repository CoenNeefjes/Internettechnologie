package client.gui;

import client.ClientApplication;
import client.service.MessageProcessor;
import general.MsgType;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

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
  private JButton joinGroupButton;
  private JButton leaveGroupButton;
  private JButton kickGroupMemberButton;
  private JButton quitButton;

  public ClientGui(MessageProcessor messageProcessor) {
    this.messageProcessor = messageProcessor;
    add(rootPanel);

    setTitle("ClientGui");
    setSize(900, 500);

    clientList.setEditable(false);
    groupList.setEditable(false);
    chatBox.setEditable(false);

    sendButton.addActionListener(this::sendMessage);
    addGroupButton.addActionListener(this::createGroup);
    joinGroupButton.addActionListener(this::joinGroup);
    leaveGroupButton.addActionListener(this::leaveGroup);
    quitButton.addActionListener(this::quit);
    kickGroupMemberButton.addActionListener(this::kickGroupMember);

    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    updateClientList();
//    updateGroupList();
  }

  /* -------------- Variables -------------- */
  private MessageProcessor messageProcessor;
  private String userName;

  /* -------------- Setters -------------- */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setRecipient(String text) {
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
      result += group;
      if (ClientApplication.subscribedGroups.contains(group)) {
        result += " (joined)";
      }
      if (ClientApplication.myGroups.contains(group)) {
        result += " (admin)";
      }
      result += "\n";
    }
    groupList.setText(result);
  }

  public void receiveMessage(MsgType msgType, String groupName, String sender, String message) {
    if (sender.equals(userName)) {
      sender = "You";
    }
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + groupName + " " + sender + ": " + message + "\n");
  }

  public void receiveMessage(MsgType msgType, String sender, String message) {
    if (sender.equals(userName)) {
      sender = "You";
    }
    chatBox.setText(
        chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + msgType + " "
            + sender + ": " + message + "\n");
  }

  /* -------------- ActionListeners -------------- */
  private void sendMessage(ActionEvent e) {
    String recipient = this.recipient.getText();
    if (recipient.equals("All")) {
      messageProcessor.sendMessage(MsgType.BCST + " " + textInput.getText());
    } else if (ClientApplication.clientNames.contains(recipient) && !recipient.equals(userName)) {
      messageProcessor.sendMessage(MsgType.PMSG + " " + recipient + " " + textInput.getText());
      chatBox.setText(
          chatBox.getText() + new SimpleDateFormat("HH:mm").format(new Date()) + " " + MsgType.PMSG
              + " "
              + "You " + "to " + recipient + ": " + textInput.getText() + "\n");

    } else if (ClientApplication.groupNames.contains(recipient)) {
      messageProcessor.sendMessage(MsgType.GMSG + " " + recipient + " " + textInput.getText());
    } else {
      errorBox("Recipient is not in any list");
      return;
    }
    textInput.setText("");
  }

  private void createGroup(ActionEvent e) {
    String groupName = groupBox();
    if (groupName != null) {
      messageProcessor.sendMessage(MsgType.CGRP + " " + groupName);
    }
  }

  private void joinGroup(ActionEvent e) {
    String groupName = groupBox();
    if (groupName != null) {
      messageProcessor.sendMessage(MsgType.JGRP + " " + groupName);
    }
  }

  private void leaveGroup(ActionEvent e) {
    String groupName = groupBox();
    if (groupName != null) {
      messageProcessor.sendMessage(MsgType.LGRP + " " + groupName);
    }
  }

  private void quit(ActionEvent e) {
    messageProcessor.sendMessage(MsgType.QUIT.toString());
    this.setVisible(false);
  }

  private void kickGroupMember(ActionEvent e) {
    String groupNameClientName = kickGroupMemberBox();
    if (groupNameClientName != null) {
      messageProcessor.sendMessage(MsgType.KGCL + " " + groupNameClientName);
    }
  }

  /* -------------- UI Components -------------- */
  public void errorBox(String infoMessage) {
    JOptionPane.showMessageDialog(null, infoMessage, "Error",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private String groupBox() {
    return JOptionPane.showInputDialog("Enter group name: ");
  }

  private String kickGroupMemberBox() {
    JTextField groupNameField = new JTextField(5);
    JTextField clientNameField = new JTextField(5);

    JPanel myPanel = new JPanel();
    myPanel.add(new JLabel("Group name:"));
    myPanel.add(groupNameField);
    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
    myPanel.add(new JLabel("Client name:"));
    myPanel.add(clientNameField);

    int result = JOptionPane.showConfirmDialog(null, myPanel,
        "Enter the name and group of the person to kick", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      return groupNameField.getText() + " " + clientNameField.getText();
    } else {
      return null;
    }
  }

  /* -------------- Logic Methods -------------- */

}
