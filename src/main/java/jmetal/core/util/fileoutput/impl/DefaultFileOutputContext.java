package jmetal.core.util.fileoutput.impl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import jmetal.core.util.errorchecking.JMetalException;
import jmetal.core.util.fileoutput.FileOutputContext;

/**
 * Class using the default method for getting a buffered writer
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class  DefaultFileOutputContext implements FileOutputContext {
  private static final String DEFAULT_SEPARATOR = "\t" ;

  protected String fileName;
  protected String separator;

  public DefaultFileOutputContext(String fileName) {
    this(fileName, DEFAULT_SEPARATOR) ;
  }

  public DefaultFileOutputContext(String fileName, String separator) {
    this.fileName = fileName ;
    this.separator = separator ;
  }

  @Override
  public BufferedWriter getFileWriter() {
    FileOutputStream outputStream ;
    try {
      outputStream = new FileOutputStream(fileName);
    } catch (FileNotFoundException e) {
      throw new JMetalException("Exception when calling method getFileWriter()", e) ;
    }
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

    return new BufferedWriter(outputStreamWriter);
  }

  @Override
  public String getSeparator() {
    return separator;
  }

  @Override
  public void setSeparator(String separator) {
    this.separator = separator;
  }

  @Override
  public String getFileName() {
    return fileName ;
  }
}
