package mekanism.common.lib;

import net.neoforged.fml.ModContainer;
import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * Version v2.0.0. Simple version handling for Mekanism.
 *
 * @param major Major number for version
 * @param minor Minor number for version
 * @param build Build number for version
 *
 * @author AidanBrady
 */
public record Version(int major, int minor, int build) implements Comparable<Version> {

    /**
     * Builds a Version object from an Artifact Version
     *
     * @implNote We don't currently include the artifact version's build number as we classify our version by major, minor, build
     */
    public Version(ArtifactVersion artifactVersion) {
        this(artifactVersion.getMajorVersion(), artifactVersion.getMinorVersion(), artifactVersion.getIncrementalVersion());
    }

    /**
     * Helper to make it so this is the only class with weird errors in IntelliJ (that don't actually exist), instead of having our main class also have "errors"
     */
    public Version(ModContainer container) {
        this(container.getModInfo().getVersion());
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

        int[] digits = new int[3];
        for (int i = 0; i < digits.length; i++) {
            try {
                digits[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return new Version(digits[0], digits[1], digits[2]);
    }

    @Override
    public int compareTo(Version version) {
        if (version.major > major) {
            return -1;
        } else if (version.major == major) {
            if (version.minor > minor) {
                return -1;
            } else if (version.minor == minor) {
                return Integer.compare(build, version.build);
            }
        }
        return 1;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + build;
    }
}