package org.jackdotjs;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;

public class Core {
  public static final String appVersion = "1.0.0-RC4";

  // user options
  public static String inputDirString;
  public static String outputDirString;
  public static boolean overwriteFiles = false;
  public static Ui.VersionItem selectedIndex;

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel( new FlatLightLaf() );

      UIManager.put("TextArea.background", Color.white);
      UIManager.put("TitlePane.centerTitle", true);
      UIManager.put("TitlePane.unifiedBackground", true);
      UIManager.put("ScrollBar.showButtons", true);
      UIManager.put("ScrollBar.width", 14);
      UIManager.put("CheckBox.arc", 6);
      UIManager.put("TextComponent.arc", 6);

      // bunch of experimental stuff
      // keeping this as i might use it later

      //UIManager.put("TextArea.foreground", new Color(25, 25, 25));
      //UIManager.put("TitlePane.background", new Color(40, 44, 52));
      //UIManager.put("TitlePane.foreground", new Color(130, 141, 150));
      //UIManager.put("TitlePane.titleMargins", new Insets(10, 0, 0, 0));


    } catch (Exception e) {
      e.printStackTrace();
    }

    Ui.init();

    String[] debugInfo = {
      "=== Debug Information ===",
      "App Version: " + appVersion,
      " ",
      "Java Version: " + System.getProperty("java.version"),
      "Java Vendor: " + System.getProperty("java.vendor"),
      "Java Vendor URL: " + System.getProperty("java.vendor.url"),
      " ",
      "OS Name: " + System.getProperty("os.name"),
      "OS Version: " + System.getProperty("os.version"),
      "OS Architecture: " + System.getProperty("os.arch"),
      " "
    };

    Ui.log(String.join("\n", debugInfo));

    Ui.log("=== Ready to extract! ===");
  }
}
