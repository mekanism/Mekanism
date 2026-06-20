package mekanism.api.chemical;

import java.util.Objects;
import net.minecraft.resources.ResourceKey;

/// Helper record to hold what amounts to a pair (clean and dirty) of keys for a specific slurry type.
///
/// @param clean Resource key representing the clean slurry.
/// @param dirty Resource key representing the dirty slurry.
///
/// @since 10.8.0
public record CleanDirtySlurryId(ResourceKey<Chemical> clean, ResourceKey<Chemical> dirty) {

    public CleanDirtySlurryId {
        Objects.requireNonNull(dirty, "Dirty slurry key cannot be null");
        Objects.requireNonNull(clean, "Clean slurry key cannot be null");
    }
}