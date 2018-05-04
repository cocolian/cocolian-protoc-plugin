package org.cocolian.maven.protoc;

/**
 * 平台探测器
 * 
 * @author 何阳
 *
 */
public interface PlatformDetector
{
  /**
   * 获取操作系统分类
   * 
   * @return String
   */
  public String getClassfier();

  /**
   * 获取标准化的操作系统信息
   * 
   * @return String
   */
  public String normalizeOs();

  /**
   * 获取标准化的操作系统指令集
   * 
   * @return String
   */
  public String normalizeArch();
}
