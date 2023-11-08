package jmetal.core.tests.qualityindicator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jmetal.core.qualityindicator.impl.NormalizedHypervolume;
import jmetal.core.util.VectorUtils;

/**
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class NormalizedHypervolumeTest {
  public static double EPSILON = 0.0000000001;

  private static String frontDirectory;
  private static String resourcesDirectory;

  @BeforeAll
  public static void startup() throws IOException {
    Properties jMetalProperties = new Properties();
    jMetalProperties.load(new FileInputStream("../jmetal.properties"));

    resourcesDirectory = "../" + jMetalProperties.getProperty("resourcesDirectory");
    frontDirectory =
        resourcesDirectory + "/" + jMetalProperties.getProperty("referenceFrontsDirectory");
  }

  @Test
  void shouldConstructorWithReferencePointCreateAValidInstance() {
    var normalizedHypervolume = new NormalizedHypervolume(new double[] {1.0, 1.0});

    Assertions.assertNotNull(normalizedHypervolume);
  }

  @Test
  void shouldEvaluateReturnZeroIfTheReferenceFrontIsEvaluatedWithItself()
      throws IOException {
    var normalizedHypervolume =
        new NormalizedHypervolume(
            VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ","));
    double[][] front = VectorUtils.readVectors("../resources/referenceFrontsCSV/ZDT1.csv", ",");

    Assertions.assertEquals(0.0, normalizedHypervolume.compute(front), EPSILON);
  }
}
