package client.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class ClientGui extends JFrame{

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

  public ClientGui() {
    add(rootPanel);

    setTitle("ClientGui");
    setSize(800, 400);
  }
}
