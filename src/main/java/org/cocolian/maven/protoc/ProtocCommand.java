package org.cocolian.maven.protoc;

import java.io.IOException;

public interface ProtocCommand {
  /**在网络上下载protoc.exe
   * @param version protoc版本
   * @param os 操作系统
   * @param arch 指令集
   * @return protoc.exe 本地文件路径
   */
  public String make(String version, String os, String arch) throws IOException;
}
