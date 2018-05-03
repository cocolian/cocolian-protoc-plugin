package org.cocolian.maven.protoc;

import java.util.Locale;
import java.util.Properties;

public class BasicPlatformDetector implements PlatformDetector
{
    public static final String DETECTED_NAME = "os.detected.name";
    public static final String DETECTED_ARCH = "os.detected.arch";
    private static final String UNKNOWN = "unknown";
    private static final String LINUX="linux";

    private static String normalizeOs(String value) {
        value = normalize(value);
        if (value.startsWith("aix")) {
            return "aix";
        }
        if (value.startsWith("hpux")) {
            return "hpux";
        }
        if (value.startsWith("os400")&&(value.length() <= 5 || !Character.isDigit(value.charAt(5)))) {
            // Avoid the names such as os4000
            return "os400";
        }
        if (value.startsWith(LINUX)) {
            return LINUX;
        }
        if (value.startsWith("macosx") || value.startsWith("osx")) {
            return "osx";
        }
        if (value.startsWith("freebsd")) {
            return "freebsd";
        }
        if (value.startsWith("openbsd")) {
            return "openbsd";
        }
        if (value.startsWith("netbsd")) {
            return "netbsd";
        }
        if (value.startsWith("solaris") || value.startsWith("sunos")) {
            return "sunos";
        }
        if (value.startsWith("windows")) {
            return "windows";
        }

        return UNKNOWN;
    }

    private static String normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64|itanium64)$")) {
            return "itanium_64";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }

        return UNKNOWN;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    public static class DetectionException extends RuntimeException {
        private static final long serialVersionUID = 7787197994442254320L;

        public DetectionException(String message) {
            super(message);
        }
    }

    @Override
    public String getClassfier() {

      final Properties allProps = new Properties(System.getProperties());

      final String osName = allProps.getProperty("os.name");
      final String osArch = allProps.getProperty("os.arch");

      final String detectedName = normalizeOs(osName);
      final String detectedArch = normalizeArch(osArch);

      final String failOnUnknownOS = allProps.getProperty("failOnUnknownOS");
      if (!"false".equalsIgnoreCase(failOnUnknownOS)) {
          if (UNKNOWN.equals(detectedName)) {
              throw new DetectionException("unknown os.name: " + osName);
          }
          if (UNKNOWN.equals(detectedArch)) {
              throw new DetectionException("unknown os.arch: " + osArch);
          }
      }

      StringBuilder detectedClassifier = new StringBuilder( detectedName + '-' + detectedArch);
      return detectedClassifier.toString();
    }
    
}