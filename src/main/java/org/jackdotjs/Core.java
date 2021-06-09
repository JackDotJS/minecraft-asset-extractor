package org.jackdotjs;

public class Core {
  public static final String appVersion = "0.1.1";

  public static void main(String[] args) {
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
