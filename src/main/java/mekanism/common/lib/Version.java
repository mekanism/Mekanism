package mekanism.common.lib;

import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * Version v2.0.0. Simple version handling for Mekanism.
 *
 * @author AidanBrady
 */
public class Version {

    /**
     * Major number for version
     */
    public int major;

    /**
     * Minor number for version
     */
    public int minor;

    /**
     * Build number for version
     */
    public int build;

    /**
     * Creates a version number with 3 digits.
     *
     * @param majorNum - major version
     * @param minorNum - minor version
     * @param buildNum - build version
     */
    public Version(int majorNum, int minorNum, int buildNum) {
        major = majorNum;
        minor = minorNum;
        build = buildNum;
    }

    /**
     * Builds a Version object from an Artifact Version
     *
     * @implNote We don't currently include the artifact version's build number as we classify our version by major, minor, build
     */
    public Version(ArtifactVersion artifactVersion) {
        this(artifactVersion.getMajorVersion(), artifactVersion.getMinorVersion(), artifactVersion.getIncrementalVersion());
    }

    /**
     * Gets a version object from a string.
     *
     * @param s - string object
     *
     * @return version if applicable, otherwise null
     */
    public static Version get(String s) {
        String[] split = s.replace('.', ':').split(":");
        if (split.length != 3) {
            return null;
        }
        for (String i : split) {
            for (char c : i.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return null;
                }
            }
        }

        int[] digits = new int[3];
        for (int i = 0; i < 3; i++) {
            digits[i] = Integer.parseInt(split[i]);
        }
        return new Version(digits[0], digits[1], digits[2]);
    }

    /**
     * Resets the version number to "0.0.0."
     */
    public void reset() {
        major = 0;
        minor = 0;
        build = 0;
    }

    /**
     * @param version Version to check against
     *
     * @return 1: greater than, 0: equal to, -1: less than
     */
    public byte comparedState(Version version) {
        if (version.major > major) {
            return -1;
        } else if (version.major == major) {
            if (version.minor > minor) {
                return -1;
            } else if (version.minor == minor) {
                return (byte) Integer.compare(build, version.build);
            }
            return 1;
        }
        return 1;
    }

    @Override
    public String toString() {
        if (major == 0 && minor == 0 && build == 0) {
            return "";
        }
        return major + "." + minor + "." + build;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + build;
        result = 31 * result + major;
        result = 31 * result + minor;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Version other = (Version) obj;
        return build == other.build && major == other.major && minor == other.minor;
    }
}