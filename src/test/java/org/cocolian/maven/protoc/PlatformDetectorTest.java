package org.cocolian.maven.protoc;

import org.junit.Before;
import org.junit.Test;

public class PlatformDetectorTest
{
  PlatformDetector platform;

  @Before
  public void setUp() throws Exception
  {
    platform = new BasicPlatformDetector();
  }

  /**
   * 获取操作系统分类
   */
  @Test
  public void normalizeOsTest()
  {
    {
      String normalizeOs = platform.normalizeOs();
      System.out.printf("操作系统信息：%s", normalizeOs);
    }
  }
  @Test
  public void getClassfierTest()
  {
    {
      String classfier = platform.getClassfier();
      System.out.printf("平台信息：%s", classfier);
    }
  }
}
