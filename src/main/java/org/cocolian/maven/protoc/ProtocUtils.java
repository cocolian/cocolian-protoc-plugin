package org.cocolian.maven.protoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtocUtils
{
  private ProtocUtils()
  {
    throw new IllegalStateException("ProtocUtils class");
  }

  public static void streamCopy(InputStream in, OutputStream out) throws IOException
  {
    int read = 0;
    byte[] buf = new byte[4096];
    while ((read = in.read(buf)) > 0)
      out.write(buf, 0, read);
  }
  public static File populateFile(String srcFilePath, File destFile) throws IOException {
    String resourcePath = "/" + srcFilePath; // resourcePath for jar, srcFilePath for test
    
    try (
            FileOutputStream os = new FileOutputStream(destFile);
            InputStream is = Protoc.class.getResourceAsStream(resourcePath)==null?new FileInputStream(srcFilePath):Protoc.class.getResourceAsStream(resourcePath);
    ){
      ProtocUtils.streamCopy(is, os);
    }
    return destFile;
}
}
