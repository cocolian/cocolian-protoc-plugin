package org.cocolian.maven.protoc;

import java.util.HashMap;
import java.util.Map;

public class Protoc {
  /**
   * protoc.exe文件路径
   */
  private String protocFilePath;
  /**
   * 参数
   */
  private String arg;
  /**
   * java文件输出路径
   */
  private String javaOut;
  /**
   * 
   */
  private String includeStdTpes;

  static String[] sStdTypesProto2 = {
      "include/google/protobuf/descriptor.proto",
  };
  
  static String[] sStdTypesProto3 = {
      "include/google/protobuf/any.proto",
      "include/google/protobuf/api.proto",
      "include/google/protobuf/descriptor.proto",
      "include/google/protobuf/duration.proto",
      "include/google/protobuf/empty.proto",
      "include/google/protobuf/field_mask.proto",
      "include/google/protobuf/source_context.proto",
      "include/google/protobuf/struct.proto",
      "include/google/protobuf/timestamp.proto",
      "include/google/protobuf/type.proto",
      "include/google/protobuf/wrappers.proto",
  };

  static Map<String,String[]> sStdTypesMap = new HashMap<>();
  
  static {
      sStdTypesMap.put("2", sStdTypesProto2);
      sStdTypesMap.put("3", sStdTypesProto3);
  }

 

  public String getProtocFilePath()
  {
    return protocFilePath;
  }

  public void setProtocFilePath(String protocFilePath)
  {
    this.protocFilePath = protocFilePath;
  }

  public String getArg()
  {
    return arg;
  }

  public void setArg(String arg)
  {
    this.arg = arg;
  }

  public String getJavaOut()
  {
    return javaOut;
  }

  public void setJavaOut(String javaOut)
  {
    this.javaOut = javaOut;
  }

  public String getIncludeStdTpes()
  {
    return includeStdTpes;
  }

  public void setIncludeStdTpes(String includeStdTpes)
  {
    this.includeStdTpes = includeStdTpes;
  }

  public static class Builder {

    private Protoc target;

    public Builder()
    {
      target = new Protoc();
    }

    public Builder command(String protocFilePath)
    {
      target.protocFilePath = protocFilePath;
      return this;
    }

    public Builder addArg(String arg)
    {
      target.arg = arg;
      return this;
    }

    public Builder includeStdTpes(String includeStdTpes)
    {
      target.includeStdTpes = includeStdTpes;
      return this;
    }

    public Builder javaOut(String javaOut)
    {
      target.javaOut = javaOut;
      return this;
    }

    public int execute()
    {
      return 0;
    }
  }

  public static void main(String[] args)
  {
    int execute = new Protoc.Builder().command("123").addArg("").includeStdTpes("").addArg("").execute();
    System.out.println(execute);
  }

}
