package mekanism.api;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ModBasedService {

    /// Modid of this service
    String modid();
}