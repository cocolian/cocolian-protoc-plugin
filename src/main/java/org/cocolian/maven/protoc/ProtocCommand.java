package org.cocolian.maven.protoc;

public interface ProtocCommand
{
  public String make(String version, String os, String arch);
}
