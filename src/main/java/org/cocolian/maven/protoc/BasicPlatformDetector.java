package org.cocolian.maven.protoc;

import java.util.Locale;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;
/**
 * 平台探测器
 * 
 * @author 何阳
 *
 */
public class BasicPlatformDetector implements PlatformDetector
{
  private static final String UNKNOWN = "unknown";
  private static final String LINUX = "linux";

  private Properties allProps = new Properties(System.getProperties());

  /**
   * 获取标准化的操作系统信息
   * 
   * @return String
   */
  public String normalizeOs()
  {
    String osName = allProps.getProperty("os.name");
    if (StringUtils.isNotBlank(osName))
    {

      String value = normalize(osName);
      if (value.startsWith("aix"))
      {
        return "aix";
      }
      if (value.startsWith("hpux"))
      {
        return "hpux";
      }
      if (value.startsWith("os400") && (value.length() <= 5 || !Character.isDigit(value.charAt(5))))
      {
        // Avoid the names such as os4000
        return "os400";
      }
      if (value.startsWith(LINUX))
      {
        return LINUX;
      }
      if (value.startsWith("macosx") || value.startsWith("osx"))
      {
        return "osx";
      }
      if (value.startsWith("freebsd"))
      {
        return "freebsd";
      }
      if (value.startsWith("openbsd"))
      {
        return "openbsd";
      }
      if (value.startsWith("netbsd"))
      {
        return "netbsd";
      }
      if (value.startsWith("solaris") || value.startsWith("sunos"))
      {
        return "sunos";
      }
      if (value.startsWith("windows"))
      {
        return "windows";
      }
    }

    return UNKNOWN;
  }

  /**
   * 获取标准化的操作系统指令集
   * 
   * @return String
   */
  public String normalizeArch()
  {
    String osArch = allProps.getProperty("os.arch");
    if (StringUtils.isNotBlank(osArch))
    {
      String value = normalize(osArch);
      if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$"))
      {
        return "x86_64";
      }
      if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$"))
      {
        return "x86_32";
      }
      if (value.matches("^(ia64|itanium64)$"))
      {
        return "itanium_64";
      }
      if (value.matches("^(sparc|sparc32)$"))
      {
        return "sparc_32";
      }
      if (value.matches("^(sparcv9|sparc64)$"))
      {
        return "sparc_64";
      }
      if (value.matches("^(arm|arm32)$"))
      {
        return "arm_32";
      }
      if ("aarch64".equals(value))
      {
        return "aarch_64";
      }
      if (value.matches("^(ppc|ppc32)$"))
      {
        return "ppc_32";
      }
      if ("ppc64".equals(value))
      {
        return "ppc_64";
      }
      if ("ppc64le".equals(value))
      {
        return "ppcle_64";
      }
      if ("s390".equals(value))
      {
        return "s390_32";
      }
      if ("s390x".equals(value))
      {
        return "s390_64";
      }
    }

    return UNKNOWN;
  }

  private String normalize(String value)
  {
    if (value == null)
    {
      return "";
    }
    return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
  }

  public class DetectionException extends RuntimeException
  {
    private static final long serialVersionUID = 7787197994442254320L;

    public DetectionException(String message)
    {
      super(message);
    }
  }

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

    StringBuilder detectedClassifier = new StringBuilder(detectedName + '-' + detectedArch);
    return detectedClassifier.toString();
  }

}
