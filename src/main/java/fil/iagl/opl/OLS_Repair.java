package fil.iagl.opl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fil.iagl.opl.synth.Synth;
import fil.iagl.opl.utils.Utils;
import fr.inria.lille.repair.common.config.Config;

public class OLS_Repair {

  public static void main(String[] args) throws IOException {
    Constantes.handleArgs(args);

    Config.INSTANCE.setCollectOnlyUsedMethod(false);

    Utils.deleteIfExist(Constantes.getSpoonedDir());
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

    new Synth().start();
    System.exit(0); // Force to kill all remaining DynaMoth' threads
  }

}
