package org.jackdotjs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Extractor {
  private static int errors = 0;
  private static int fileCount = 0;

  private static void handleError(IOException e) {
    errors++;

    String[] errMsg = {
            "Error: Failed to extract file",
            "Reason: " + e
    };

    Ui.log(String.join("\n", errMsg));
    e.printStackTrace();
  }

  public static void extract() {
    Ui.log("Starting...");
    Ui.forceDisable();

    long startTime = System.nanoTime();

    File outputDir = new File(Core.outputDirString + File.separator + "mcassets_extracted");

    if (!outputDir.exists()) outputDir.mkdirs();

    Ui.log("Extraction path: " + outputDir.getAbsolutePath());
    Ui.log("Getting index from " + Core.selectedIndex);

    // new thread allows extraction process to run separately
    //
    // this prevents the UI from freezing up after hitting the start button,
    // and also decreases the perceived processing time, since otherwise the
    // UI would be frozen even long after the extraction process has finished.
    Thread extract = new Thread(() -> {
      try {
        StringBuilder fileLines = new StringBuilder();
        Stream<String> fileStream = Files.lines(Core.selectedIndex.getFile().toPath(), StandardCharsets.UTF_8);
        fileStream.forEach(fileLines::append);

        String indexContent = fileLines.toString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode index = mapper.readTree(indexContent).get("objects");

        fileCount = index.size();

        for (Iterator<String> node = index.fieldNames(); node.hasNext(); ) {
          String key = node.next();

          Ui.log("Extracting \"" + key + "\"...");

          String hash = index.get(key).get("hash").asText();
          File copyPath = new File(
            outputDir.getAbsolutePath()
            + File.separator
            + Core.selectedIndex.getLabel().replaceFirst("\\.[^.]+$", "")
            + File.separator
            + key.replaceFirst("[^\\/]+$", "")
          );

          copyPath.mkdirs();

          String loc = hash.substring(0, 2);

          File objPath = new File(
        Core.inputDirString
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

              if (Core.overwriteFiles) {
                Files.copy(objPath.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
              } else {
                Files.copy(objPath.toPath(), dest);
              }
            } catch (IOException e) {
              handleError(e);
            }
          }
        }
      } catch (IOException e) {
        handleError(e);
      }

      long totalTime = System.nanoTime() - startTime;

      String[] doneMsg = {
        "=== Finished! ===",
        "Files processed: " + fileCount,
        "Errors: " + errors,
        "Total time taken: " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.NANOSECONDS) + "s"
      };

      Ui.log(String.join("\n", doneMsg));

      errors = 0;
      fileCount = 0;

      Ui.forceEnable();
    });

    // pause purely to give some visual feedback before spamming
    // the console with the bulk of the extraction process
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        extract.start();
      }
    }, 1000);
  }
}
