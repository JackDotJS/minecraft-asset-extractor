package org.jackdotjs;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Ui {
  public static final JTextField inputDirText = new JTextField();
  public static final JTextField outputDirText = new JTextField();
  public static final JComboBox<VersionItem> versions = new JComboBox<>();

  private static String inputDirTextMemory;
  private static String outputDirTextMemory;
  private static File indexesDir;

  private static final JFrame window = new JFrame();
  private static final JPanel windowWrapper = new JPanel(new BorderLayout());
  private static final JButton inputDirSelect = new JButton("...");
  private static final JButton outputDirSelect = new JButton("...");
  private static final JButton start = new JButton("Start");
  private static final JButton options = new JButton("Options");
  private static final JTextArea consoleText = new JTextArea();
  private static final JPopupMenu rc_edit = new JPopupMenu();
  private static final JPopupMenu rc_noedit = new JPopupMenu();
  private static final Color invalidCol = new Color(255, 190, 190);
  private static final Color warnCol = new Color(225, 225, 255);

  public static final class VersionItem {
    private String label;
    private File file;

    public VersionItem(String label, File file) {
      this.label = label;
      this.file = file;
    }

    public String getLabel() {
      return this.label;
    }

    public File getFile() {
      return this.file;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public static void forceEnable() {
    inputDirText.setEnabled(true);
    inputDirSelect.setEnabled(true);
    outputDirText.setEnabled(true);
    outputDirSelect.setEnabled(true);
    versions.setEnabled(true);
    options.setEnabled(true);
    start.setEnabled(true);

    validateInputs(); // just to be sure
  }

  public static void forceDisable() {
    inputDirText.setEnabled(false);
    inputDirSelect.setEnabled(false);
    outputDirText.setEnabled(false);
    outputDirSelect.setEnabled(false);
    versions.setEnabled(false);
    start.setEnabled(false);
    options.setEnabled(false);
  }

  public static void log(String message) {
    DateTimeFormatter tsFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    String timestamp = "[" + LocalTime.now().format(tsFormat) + "] ";
    String[] lines = message.split("[\\r\\n]");

    String space = " ";

    StringJoiner linesCorrected = new StringJoiner("\n" + String.join("", Collections.nCopies(timestamp.length(), space)));

    for (String l:lines) linesCorrected.add(l);

    consoleText.append(timestamp + linesCorrected + "\n");
    System.out.println(timestamp + linesCorrected);

    consoleText.setCaretPosition(consoleText.getDocument().getLength());
  }

  private static void openBrowser(String dirType) {
    JFileChooser selDir = new JFileChooser();
    selDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    selDir.setAcceptAllFileFilterUsed(false); // disable the "All files" option.
    selDir.setApproveButtonToolTipText("Select the current folder");

    if (isInput(dirType)) {
      selDir.setDialogTitle("Select Minecraft Directory");

      File inputDir = new File(inputDirText.getText());
      if (inputDir.isDirectory()) selDir.setCurrentDirectory(inputDir);
    } else {
      selDir.setDialogTitle("Select Output Directory");

      File outputDir = new File(outputDirText.getText());
      if (outputDir.isDirectory()) selDir.setCurrentDirectory(outputDir);
    }

    int result = selDir.showDialog(window, "Select Folder");

    if (result == JFileChooser.APPROVE_OPTION) {
      if (isInput(dirType)) {
        inputDirText.setText(selDir.getSelectedFile().getAbsolutePath());
      } else {
        outputDirText.setText(selDir.getSelectedFile().getAbsolutePath());
      }

      validateInputs();
    }
  }

  private static String getMinecraftDir() {
    String[] dirStrings = new String[0];

    if (SystemUtils.IS_OS_WINDOWS) {
      dirStrings = new String[] {
              System.getProperty("user.home"),
              "AppData",
              "Roaming",
              ".minecraft"
      };
    } else if (SystemUtils.IS_OS_LINUX) {
      dirStrings = new String[] {
              System.getProperty("user.home"),
              ".minecraft"
      };
    } else if (SystemUtils.IS_OS_MAC) {
      dirStrings = new String[] {
              System.getProperty("user.home"),
              "Library",
              "Application Support",
              "minecraft"
      };
    }

    return String.join(File.separator, dirStrings);
  }

  private static void getDefaultDirs() {
    String newDir = getMinecraftDir();

    File newDirFile = new File(newDir);

    if (newDir.length() == 0 || !newDirFile.isDirectory()) {
      String msg = "Could not find default Minecraft directory. Please select the directory.";

      JOptionPane.showMessageDialog(Ui.window, msg, "Warning", JOptionPane.WARNING_MESSAGE);

      inputDirText.setText(System.getProperty("user.dir"));
    } else {
      inputDirText.setText(newDirFile.getAbsolutePath());
    }

    outputDirText.setText(System.getProperty("user.dir"));

    inputDirTextMemory = inputDirText.getText();
    outputDirTextMemory = outputDirText.getText();
  }

  private static void getIndexes() {
    versions.removeAllItems();

    File[] indexFiles = indexesDir.listFiles();

    assert indexFiles != null;

    ArrayUtils.reverse(indexFiles);

    for (File index : indexFiles) {
      if (index.isFile() && index.getName().endsWith(".json")) {
        VersionItem ver = new VersionItem(index.getName(), index);
        versions.addItem(ver);
      }
    }
  }

  private static void validateInputs() {
    File inputDir = new File(inputDirText.getText());
    File outputDir = new File(outputDirText.getText());

    String[] indexStr = { inputDirText.getText(), "assets", "indexes" };
    indexesDir = new File(String.join(File.separator, indexStr));

    boolean disableStart = false;
    boolean disableVersions = false;

    if (inputDir.isDirectory()) {
      inputDirText.setBackground(Color.white);
    } else {
      disableStart = true;
      disableVersions = true;
      inputDirText.setBackground(invalidCol);
    }

    if (outputDir.isDirectory()) {
      outputDirText.setBackground(Color.white);
    } else {
      outputDirText.setBackground(warnCol);
    }

    if (!indexesDir.isDirectory() || indexesDir.listFiles().length == 0) {
      inputDirText.setBackground(invalidCol);
      disableStart = true;
      disableVersions = true;
    } else {
      inputDirText.setBackground(Color.white);
    }

    versions.setEnabled(!disableVersions);
    start.setEnabled(!disableStart);

    inputDirTextMemory = inputDirText.getText();
    outputDirTextMemory = outputDirText.getText();

    if (!disableVersions) getIndexes();
  }

  private static boolean isInput(String dirType) {
    return dirType.equalsIgnoreCase("input");
  }

  private static void setWindow() {
    window.setTitle("Jack's MCAsset Extractor");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLocationRelativeTo(null);
    window.setSize(600, 450);
    window.setMinimumSize(new Dimension(400, 300));

    URL resourceIcon = Ui.class.getClassLoader().getResource("img/icon.png");

    window.setIconImage(Toolkit.getDefaultToolkit().getImage(resourceIcon));
  }

  private static void setContextMenu() {
    CopyAction rc_copy = new DefaultEditorKit.CopyAction();
    rc_copy.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
    rc_copy.putValue(AbstractAction.NAME, "Copy");

    CutAction rc_cut = new DefaultEditorKit.CutAction();
    rc_cut.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
    rc_cut.putValue(AbstractAction.NAME, "Cut");

    PasteAction rc_paste = new DefaultEditorKit.PasteAction();
    rc_paste.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
    rc_paste.putValue(AbstractAction.NAME, "Paste");

    rc_edit.add(rc_copy);
    rc_edit.add(rc_cut);
    rc_edit.add(rc_paste);

    rc_noedit.add(rc_copy);
  }

  private static JPanel setDirectoriesPanel() {
    // create directories wrapper panel
    JPanel dirs = new JPanel();
    dirs.setLayout(new BoxLayout(dirs, BoxLayout.PAGE_AXIS));

    // create input directory panel
    JPanel inputDirPanel = new JPanel();
    inputDirPanel.setLayout(new BoxLayout(inputDirPanel, BoxLayout.LINE_AXIS));
    inputDirPanel.setMaximumSize(new Dimension(580, 400));
    JLabel inputDirLabel = new JLabel("Minecraft Directory", SwingConstants.RIGHT);
    inputDirText.setComponentPopupMenu(rc_edit);
    inputDirSelect.setFocusPainted(false);
    inputDirSelect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    inputDirText.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        System.out.println("focus gained");
      }

      @Override
      public void focusLost(FocusEvent e) {
        System.out.println("focus lost");
        if (!inputDirTextMemory.equals(inputDirText.getText())) {
          validateInputs();
          System.out.println("text changed");
        }
      }
    });

    inputDirText.addActionListener(e -> {
      System.out.println("action listener");
      validateInputs();
    });

    inputDirText.setBorder(BorderFactory.createCompoundBorder(
            inputDirText.getBorder(),
            BorderFactory.createEmptyBorder(0,5,0,5)
    ));

    inputDirSelect.addActionListener(e -> openBrowser("input"));

    inputDirPanel.add(inputDirLabel);
    inputDirPanel.add(Box.createRigidArea(new Dimension(10,0)));
    inputDirPanel.add(inputDirText);
    inputDirPanel.add(Box.createRigidArea(new Dimension(10,0)));
    inputDirPanel.add(inputDirSelect);
    inputDirPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

    // create output directory panel
    JPanel outputDirPanel = new JPanel();
    outputDirPanel.setLayout(new BoxLayout(outputDirPanel, BoxLayout.LINE_AXIS));
    outputDirPanel.setMaximumSize(new Dimension(580, 400));
    JLabel outputDirLabel = new JLabel("Output Directory", SwingConstants.RIGHT);
    JLabel outputInfo = new JLabel("<HTML><U>(?)</U></HTML>");
    outputDirText.setComponentPopupMenu(rc_edit);
    outputDirText.setPreferredSize(new Dimension(500, outputDirText.getPreferredSize().height));
    outputDirSelect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    outputDirSelect.setFocusPainted(false);

    String[] outputInfoText = {
            "<HTML><p width=\"300\">",
            "Note that, for the sake of avoiding a huge mess, all extracted",
            "files will be placed in an additional folder called <b>\"extracted\"</b>",
            "within the chosen directory.",
            "<br>",
            "<br>",
            "The full directory path will also be automatically created,",
            "if it does not exist.",
            "</span></HTML>"
    };

    outputInfo.setToolTipText(String.join(" ", outputInfoText));

    outputDirText.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (!outputDirTextMemory.equals(outputDirText.getText())) {
          validateInputs();
        }
      }
    });

    outputDirText.addActionListener(e -> validateInputs());

    outputDirText.setBorder(BorderFactory.createCompoundBorder(
            outputDirText.getBorder(),
            BorderFactory.createEmptyBorder(0,5,0,5)
    ));

    outputDirSelect.addActionListener(e -> openBrowser("output"));

    outputDirPanel.add(outputDirLabel);
    outputDirPanel.add(Box.createRigidArea(new Dimension(5,0)));
    outputDirPanel.add(outputInfo);
    outputDirPanel.add(Box.createRigidArea(new Dimension(10,0)));
    outputDirPanel.add(outputDirText);
    outputDirPanel.add(Box.createRigidArea(new Dimension(10,0)));
    outputDirPanel.add(outputDirSelect);
    outputDirPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    dirs.add(inputDirPanel);
    dirs.add(outputDirPanel);

    return dirs;
  }

  private static JPanel setConsolePanel() {
    JPanel log = new JPanel();
    log.setLayout(new BorderLayout());
    log.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

    Font consoleFont = new Font("monospaced", Font.PLAIN, 12);
    consoleText.setFont(consoleFont);
    consoleText.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
    consoleText.setEditable(false);
    consoleText.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    consoleText.setComponentPopupMenu(rc_noedit);

    JScrollPane logPanel = new JScrollPane(consoleText);
    logPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    logPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    log.add(logPanel, BorderLayout.CENTER);

    return log;
  }

  private static JPanel setControlPanel() {
    // control panel wrapper
    JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
    controls.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    // create version selector
    JPanel selectVer = new JPanel();
    selectVer.setLayout(new BoxLayout(selectVer, BoxLayout.LINE_AXIS));
    selectVer.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
    selectVer.setMaximumSize(new Dimension(300, 400));

    JLabel selectVerLabel = new JLabel("Minecraft Index", SwingConstants.RIGHT);

    versions.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    DefaultListCellRenderer rlist = new DefaultListCellRenderer();
    rlist.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
    versions.setRenderer(rlist);

    selectVer.add(selectVerLabel);
    selectVer.add(Box.createRigidArea(new Dimension(10,0)));
    selectVer.add(versions);

    // create button row
    Box bRow = Box.createHorizontalBox();
    bRow.setMaximumSize(new Dimension(560, 400));

    // create about menu
    JButton about = new JButton("About");
    about.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    about.setFocusPainted(false);
    about.setPreferredSize(new Dimension(100, about.getPreferredSize().height));

    URL resJackIcon = Ui.class.getClassLoader().getResource("img/jack.png");
    ImageIcon jackIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(resJackIcon));

    JLabel copyStyle = new JLabel();
    Font aboutFont = copyStyle.getFont();

    int fontColR = copyStyle.getForeground().getRed();
    int fontColG= copyStyle.getForeground().getGreen();
    int fontColB = copyStyle.getForeground().getBlue();

    String[] style = {
      "font-family:" + aboutFont.getFamily() + ";",
      "font-weight:" + ((aboutFont.isBold()) ? "bold" : "normal") + ";",
      "font-size:" + aboutFont.getSize() + "pt;",
      "color:rgb(" + fontColR + "," + fontColG + "," + fontColB + ");",
      "user-select: none"
    };

    String[] aboutText = {
      "<html><body style=\"" + String.join("", style) + "\">",
      "<h1 style=\"margin: 0\">Jack's MCAsset Extractor</h1>",
      "Version " + Core.appVersion,
      "",
      "i made dis C:",
      "",
      "You can find this project on GitHub:",
      "<a href=\"https://github.com/JackDotJS/jacks-mcasset-extractor\">https://github.com/JackDotJS/jacks-mcasset-extractor</a>",
      "",
      "Special thanks to <a href=\"https://github.com/TropheusJ\">Tropheus Jay</a> for helping me",
      "navigate this nightmarish programming language &lt;3",
      "",
      "</body></html>"
    };

    JEditorPane aboutTextPanel = new JEditorPane("text/html", String.join("\n<br>", aboutText));

    aboutTextPanel.setHighlighter(null);
    aboutTextPanel.setEditable(false);
    aboutTextPanel.setBackground(copyStyle.getBackground());
    aboutTextPanel.addHyperlinkListener(e -> {
      if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
        try {
          Desktop.getDesktop().browse(e.getURL().toURI());
        } catch (IOException | URISyntaxException ioException) {
          ioException.printStackTrace();
        }
      }
    });

    about.addActionListener(e -> JOptionPane.showMessageDialog(
      Ui.window,
      aboutTextPanel,
      "About",
      JOptionPane.INFORMATION_MESSAGE, jackIcon)
    );

    // set up options menu
    options.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    options.setFocusPainted(false);
    options.setPreferredSize(new Dimension(100, options.getPreferredSize().height));

    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

    Box optRow1 = Box.createHorizontalBox();
    JLabel optOverrideLabel = new JLabel("Overwrite Existing Files");
    JLabel optOverrideInfo = new JLabel("<HTML><U>(?)</U></HTML>");
    optOverrideInfo.setToolTipText(String.join(" ", new String[] {
      "<html><p width=\"300\">",
      "Forces extracted files to overwrite any existing",
      "file with the same name in the chosen directory.",
      "</p></html>"
    }));
    JCheckBox optOverrideCtrl = new JCheckBox();
    optOverrideCtrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    optOverrideCtrl.addActionListener(e -> Core.overwriteFiles = optOverrideCtrl.isSelected());

    optRow1.add(optOverrideLabel);
    optRow1.add(Box.createRigidArea(new Dimension(5,5)));
    optRow1.add(optOverrideInfo);
    optRow1.add(Box.createHorizontalGlue());
    optRow1.add(optOverrideCtrl);

    Box optMsgRow = Box.createHorizontalBox();
    JLabel optMsg = new JLabel("More options coming soon!");
    optMsg.setForeground(Color.gray);

    optMsgRow.add(optMsg);

    optionsPanel.add(optMsgRow);
    optionsPanel.add(Box.createRigidArea(new Dimension(10,10)));
    optionsPanel.add(optRow1);
    optionsPanel.add(Box.createRigidArea(new Dimension(10,5)));

    options.addActionListener(e -> JOptionPane.showMessageDialog(
      Ui.window,
      optionsPanel,
      "Options",
      JOptionPane.PLAIN_MESSAGE)
    );

    // set up start button
    start.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    start.setFocusPainted(false);
    start.setPreferredSize(new Dimension(100, start.getPreferredSize().height));

    start.addActionListener(e -> {
      Core.inputDirString = inputDirText.getText();
      Core.outputDirString = outputDirText.getText();
      Core.selectedIndex = (VersionItem) versions.getSelectedItem();

      Extractor.extract();
    });

    bRow.add(options);
    bRow.add(Box.createHorizontalGlue());
    bRow.add(about);
    bRow.add(Box.createHorizontalGlue());
    bRow.add(start);

    controls.add(selectVer);
    controls.add(bRow);

    return controls;
  }

  public static void init() {
    setWindow();
    setContextMenu();
    JPanel dirs = setDirectoriesPanel();
    JPanel console = setConsolePanel();
    JPanel controls = setControlPanel();

    ToolTipManager.sharedInstance().setInitialDelay(100);
    ToolTipManager.sharedInstance().setDismissDelay(1234567890); // a really long time

    getDefaultDirs();
    validateInputs();

    windowWrapper.add(dirs, BorderLayout.NORTH);
    windowWrapper.add(console, BorderLayout.CENTER);
    windowWrapper.add(controls, BorderLayout.SOUTH);

    window.getContentPane().add(windowWrapper);
    window.setVisible(true);

    // brings window to front after opening
    window.setAlwaysOnTop(true);
    window.toFront();
    window.requestFocus();
    window.setAlwaysOnTop(false);
  }
}
