package fil.iagl.opl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fil.iagl.opl.synth.Synth;
import fil.iagl.opl.utils.Params;
import fil.iagl.opl.utils.Utils;

/**
 * @author Maxime CLEMENT
 *
 * Entry point
 */
public class OLS_Test {

  public static void main(String[] args) throws IOException {
    // Parse arguments from user
    Params params = new Params(args);

    Utils.deleteIfExist(Constantes.SPOONED_FOLDER_NAME);

    // Create duplicate folder if -o is not specified
    if (!params.getOverride()) {
      String workingDirPath = params.getProjectPath() + "_synth";
      File workingDir = new File(workingDirPath);
      File projectDir = new File(params.getProjectPath());
      try {
        Utils.deleteIfExist(workingDir);
        FileUtils.copyDirectory(projectDir, workingDir);
        params.setProjectPath(workingDirPath);
      } catch (IOException e) {
        throw new RuntimeException("Error occured when creating temporary directory", e);
      }
    }

    // Run the synthesizer
    new Synth(params).start();
    System.exit(0); // Force to kill all remaining DynaMoth' threads
  }

}
