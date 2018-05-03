package org.cocolian.maven.protoc;

import org.junit.Test;

public class PlatformDetectorTest
{
  @Test
  public void getClassfierTest()
  {
    {
      PlatformDetector platform = new BasicPlatformDetector();
      String classfier = platform.getClassfier();
      System.out.printf("平台信息：%s", classfier);
    }
  }
}
