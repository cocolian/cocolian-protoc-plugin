package org.cocolian.maven.protoc;

import org.junit.Before;
import org.junit.Test;
public class ProtocTest {
  ProtocCommand protocCommand = null;
  PlatformDetector platformDetector = null;
  
  static final String sPersonSchemaFile = "src/test/resources/PersonSchema.proto";
  static final String sStdTypeExampleFile2 = "src/test/resources/StdTypeExample2.proto";
  static final String sStdTypeExampleFile3 = "src/test/resources/StdTypeExample3.proto";
  
  @Before
  public void setUp() throws Exception
  {
    platformDetector = new BasicPlatformDetector();
    protocCommand = new RemoteProtocCommand();
  } 

  @Test
  public void testRunProtocBasic() throws Exception
  {
    String os = platformDetector.normalizeOs();
    String arch = platformDetector.normalizeArch();
    String command = protocCommand.make("2.4.1", os, arch);
    int execute = new Protoc.Builder().command(command).addArg(sPersonSchemaFile).includeStdTpes("").javaOut("target/test-protoc").execute();
    System.out.println(execute);

  }

}
