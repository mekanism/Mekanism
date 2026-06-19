package mekanism.api.chemical;

import net.minecraft.resources.ResourceKey;

/// @param dirty Resource key representing the dirty slurry.
/// @param clean Resource key representing the clean slurry.
///
/// @since 10.8.0
public record CleanDirtySlurryId(ResourceKey<Chemical> dirty, ResourceKey<Chemical> clean) {
}