package org.jackdotjs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Extractor {
  public static void extract() {
    Ui.log("Starting...");
    Ui.forceDisable();

    long startTime = System.nanoTime();

    Ui.VersionItem selVer = (Ui.VersionItem) Ui.versions.getSelectedItem();

    File outputDir = new File(Ui.outputDirText.getText() + File.separator + "mcassets_extracted");

    if (!outputDir.exists()) outputDir.mkdirs();

    Ui.log("Extraction path: " + outputDir.getAbsolutePath());
    Ui.log("Getting index from " + selVer);

    Thread extract = new Thread(() -> {
      int errors = 0;

      try {
        String indexContent = Files.readString(selVer.getFile().toPath());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode index = mapper.readTree(indexContent).get("objects");

        for (Iterator<String> node = index.fieldNames(); node.hasNext(); ) {
          String key = node.next();

          Ui.log("Extracting \"" + key + "\"...");

          String hash = index.get(key).get("hash").asText();
          File copyPath = new File(
                  outputDir.getAbsolutePath()
                  + File.separator
                  + selVer.getLabel().replaceFirst("\\.[^.]+$", "")
                  + File.separator
                  + key.replaceFirst("[^\\/]+$", "")
          );

          copyPath.mkdirs();

          String loc = hash.substring(0, 2);

          File objPath = new File(
                  Ui.inputDirText.getText()
                          + File.separator
                          + "assets"
                          + File.separator
                          + "objects"
                          + File.separator
                          + loc
                          + File.separator
                          + hash
          );

          if (!objPath.exists()) {
            Ui.log("Error: Could not find object \"" + key + "\"");
          } else {
            try {
              Path dest = Paths.get(copyPath.getAbsolutePath() + File.separator + key.replaceFirst("^.+[\\/]", ""));
              System.out.println(dest);
              Files.copy(objPath.toPath(), dest);
            } catch (IOException e) {
              errors++;

              String[] errMsg = {
                      "Error: Failed to copy object \"" + key + "\"",
                      "Reason: " + e
              };

              Ui.log(String.join("\n", errMsg));
              e.printStackTrace();
            }

          }
        }

        long totalTime = System.nanoTime() - startTime;

        String[] doneMsg = {
                "=== Finished! ===",
                "Files processed: " + index.size(),
                "Errors: " + errors,
                "Total time taken: " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.NANOSECONDS) + "s"
        };


        Ui.log(String.join("\n", doneMsg));
        Ui.forceEnable();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // pause purely to give some visual feedback before potentially spamming the console
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        extract.start();
      }
    }, 1000);
  }
}
