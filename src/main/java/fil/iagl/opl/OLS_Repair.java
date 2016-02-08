package fil.iagl.opl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fil.iagl.opl.synth.Synth;
import fil.iagl.opl.utils.Params;
import fil.iagl.opl.utils.Utils;
import fr.inria.lille.repair.common.config.Config;

/**
 * @author Maxime CLEMENT
 *
 * Entry point
 */
public class OLS_Repair {

  public static void main(String[] args) throws IOException {
    // Parse arguments from user
    Params.handleArgs(args);

    // DynaMoth Conf
    Config.INSTANCE.setCollectOnlyUsedMethod(false);

    Utils.deleteIfExist(Constantes.getSpoonedDir());

    // Create duplicate folder if -o is not specified
    if (!Constantes.getOverride()) {
      String workingDirPath = Constantes.getProjectPath() + "_synth";
      File workingDir = new File(workingDirPath);
      File projectDir = new File(Constantes.getProjectPath());
      try {
        Utils.deleteIfExist(workingDir);
        if (workingDir.exists()) {
          FileUtils.forceDelete(workingDir);
        }
        FileUtils.copyDirectory(projectDir, workingDir);
        Constantes.setProjectPath(workingDirPath);
      } catch (IOException e) {
        throw new RuntimeException("Error occured when creating temporary directory", e);
      }
    }

    // Run the synthesizer
    new Synth().start();
    System.exit(0); // Force to kill all remaining DynaMoth' threads
  }

}
