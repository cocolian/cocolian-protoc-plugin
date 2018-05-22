package org.cocolian.maven.protoc;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ProtocCommandTest
{

  ProtocCommand protocCommand = null;
  PlatformDetector platformDetector = null;

  @Before
  public void setUp() throws Exception
  {
    platformDetector = new BasicPlatformDetector();
    protocCommand = new RemoteProtocCommand();
  }

  @Test
  public void makeTest()
  {
    String os = platformDetector.normalizeOs();
    String arch = platformDetector.normalizeArch();
    try
    {
      protocCommand.make("2.4.1", os, arch);
      protocCommand.make("2.5.0", os, arch);
      protocCommand.make("2.6.1", os, arch);
      protocCommand.make("3.4.0", os, arch);
      protocCommand.make("3.5.0", os, arch);
      protocCommand.make("3.5.1", os, arch);
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
