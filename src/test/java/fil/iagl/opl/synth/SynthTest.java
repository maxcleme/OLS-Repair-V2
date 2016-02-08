package fil.iagl.opl.synth;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

import fil.iagl.opl.Constantes;
import fil.iagl.opl.utils.Params;
import fil.iagl.opl.utils.Utils;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.NameFilter;

public class SynthTest {

  @Test
  public void testName() throws Exception {
    String[] args = {
      "-s", "test-projects/DynaMoth",
      "-m", "C:/Users/RMS/Downloads/apache-maven-3.3.3-bin/apache-maven-3.3.3",
      "-c", "0", "-c", "-1", "-c", "1", "-c", "2",
    };

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

    new Synth(params).start();

    Launcher l = new Launcher();
    l.addInputResource("test-projects/DynaMoth_synth/src/main/java");
    l.buildModel();

    CtClass<?> sample = (CtClass<?>) l.getFactory().Package().getRootPackage().getElements(new NameFilter<>("Sample")).get(0);

    CtMethod<?> addMethod = sample.getMethod("add", l.getFactory().Type().INTEGER_PRIMITIVE, l.getFactory().Type().INTEGER_PRIMITIVE);
    CtMethod<?> minusMethod = sample.getMethod("minus", l.getFactory().Type().INTEGER_PRIMITIVE, l.getFactory().Type().INTEGER_PRIMITIVE);
    CtMethod<?> timesMethod = sample.getMethod("times", l.getFactory().Type().INTEGER_PRIMITIVE, l.getFactory().Type().INTEGER_PRIMITIVE);
    CtMethod<?> squareMethod = sample.getMethod("square", l.getFactory().Type().INTEGER_PRIMITIVE);
    CtMethod<?> isEvenMethod = sample.getMethod("isEven", l.getFactory().Type().INTEGER_PRIMITIVE);
    CtMethod<?> isOddMethod = sample.getMethod("isOdd", l.getFactory().Type().INTEGER_PRIMITIVE);

    Assertions.assertThat(addMethod.getBody().getStatement(0).toString()).isEqualTo("return a + b");
    Assertions.assertThat(minusMethod.getBody().getStatement(0).toString()).isEqualTo("return a - b");
    Assertions.assertThat(timesMethod.getBody().getStatement(0).toString()).isEqualTo("return a * b");
    Assertions.assertThat(squareMethod.getBody().getStatement(0).toString()).isEqualTo("return times(a, minus(a, 0))");
    Assertions.assertThat(isEvenMethod.getBody().getStatement(0).toString()).isEqualTo("return !(isOdd(a))");
    Assertions.assertThat(isOddMethod.getBody().getStatement(0).toString()).isEqualTo("return 0 != (a % 2)");
  }

}
