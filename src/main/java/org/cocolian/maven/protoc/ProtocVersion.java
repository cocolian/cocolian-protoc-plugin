package org.cocolian.maven.protoc;

public class ProtocVersion {
  private final String mGroup;
  private final String mArtifact;
  private final String mVersion;

  public static ProtocVersion getVersion(String spec)
  {
    if (!spec.startsWith("-v"))
    {
      return null;
    }
    ProtocVersion version = null;
    String[] as = spec.split(":");
    if (as.length == 4 && as[0].equals("-v"))
    {
      version = new ProtocVersion(as[1], as[2], as[3]);
    } else
    {
      version = new ProtocVersion(null, null, spec.substring(2));
    }
    if (version.mVersion.length() == 3)
    { // "123" -> "1.2.3"
      String dotVersion = version.mVersion.charAt(0) + "." + version.mVersion.charAt(1) + "." + version.mVersion.charAt(2);
      version = new ProtocVersion(version.mGroup, version.mArtifact, dotVersion);
    }
    return version;
  }

  public ProtocVersion(String group, String artifact, String version)
  {
    mGroup = group;
    mArtifact = artifact;
    mVersion = version;
  }

  public String getmGroup()
  {
    return mGroup;
  }

  public String getmArtifact()
  {
    return mArtifact;
  }

  public String getmVersion()
  {
    return mVersion;
  }

  @Override
  public String toString()
  {
    if (mArtifact == null)
    {
      return mVersion;
    }
    return mGroup + ":" + mArtifact + ":" + mVersion;
  }
}
