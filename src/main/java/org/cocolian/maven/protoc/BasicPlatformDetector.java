package org.cocolian.maven.protoc;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;

/**
 * 平台探测器
 * 
 * @author 何阳
 *
 */
public class BasicPlatformDetector implements PlatformDetector {
  private static final String UNKNOWN = "unknown";
  /**
   * 操作系统集合
   */
  private static final Map<String, String> osMap = new HashMap<>();
  /**
   * 指令集集合
   */
  private static final Map<String, String> archMap = new HashMap<>();
  private Properties allProps = new Properties(System.getProperties());

  static{
    // 初始化操作系统集合
    osMap.put("aix", "aix");
    osMap.put("hpux", "hpux");
    osMap.put("os400", "os400");
    osMap.put("linux", "linux");
    osMap.put("macosx", "osx");
    osMap.put("osx", "osx");
    osMap.put("freebsd", "freebsd");
    osMap.put("openbsd", "openbsd");
    osMap.put("netbsd", "netbsd");
    osMap.put("solaris", "sunos");
    osMap.put("sunos", "sunos");
    osMap.put("windows", "windows");

    // 初始化指令集集合
    archMap.put("^(x8664|amd64|ia32e|em64t|x64)$", "x86_64");
    archMap.put("^(x8632|x86|i[3-6]86|ia32|x32)$", "x86_32");
    archMap.put("^(ia64|itanium64)$", "itanium_64");
    archMap.put("^(sparc|sparc32)$", "sparc_32");
    archMap.put("^(sparcv9|sparc64)$", "sparc_64");
    archMap.put("^(arm|arm32)$", "arm_32");
    archMap.put("^(aarch64)$", "aarch_64");
    archMap.put("^(ppc|ppc32)$", "ppc_32");
    archMap.put("^(ppc64)$", "ppc_64");
    archMap.put("^(ppc64le)$", "ppcle_64");
    archMap.put("^(s390)$", "s390_32");
    archMap.put("^(s390x)$", "s390_64");
  }

  /**
   * 获取标准化的操作系统信息
   * 
   * @return String
   */
  public String normalizeOs()
  {
    String osName = allProps.getProperty("os.name");
    String osStr = UNKNOWN;
    if (StringUtils.isNotBlank(osName))
    {
      String value = normalize(osName);
      for (Map.Entry<String, String> entry : osMap.entrySet())
      {
        {
          String key = entry.getKey();
          if (value.startsWith(key))
          {
            osStr = osMap.get(key);
          }
        }
      }
    }
    return osStr;
  }

  /**
   * 获取标准化的操作系统指令集
   * 
   * @return String
   */
  public String normalizeArch()
  {
    String osArch = allProps.getProperty("os.arch");
    String archStr = UNKNOWN;
    if (StringUtils.isNotBlank(osArch))
    {
      String value = normalize(osArch);
      for (Map.Entry<String, String> entry : archMap.entrySet())
      {
        String key = entry.getKey();
        if (value.matches(key))
        {
          archStr = archMap.get(key);
        }
      }
    }

    return archStr;
  }

  private String normalize(String value)
  {
    if (value == null)
    {
      return "";
    }
    return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
  }

  /**
   * 获取操作系统分类
   */
  @Override
  public String getClassfier()
  {
    final String failOnUnknownOS = allProps.getProperty("failOnUnknownOS");
    final String detectedName = normalizeOs();
    final String detectedArch = normalizeArch();
    if (!"false".equalsIgnoreCase(failOnUnknownOS))
    {
      if (UNKNOWN.equals(detectedName))
      {
        throw new DetectionException("unknown os.name: " + allProps.getProperty("os.name"));
      }
      if (UNKNOWN.equals(detectedArch))
      {
        throw new DetectionException("unknown os.arch: " + allProps.getProperty("os.arch"));
      }
    }

    StringBuilder detectedClassifier = new StringBuilder(detectedName);
    detectedClassifier.append("-");
    detectedClassifier.append(detectedArch);
    return detectedClassifier.toString();
  }
  
}
