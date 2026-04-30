package mekanism.datagen.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.nio.file.Path;
import java.util.Collection;
import mekanism.common.PersistingDisabledProvidersProvider;
import net.minecraft.WorldVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataGenerator.Cached.class)
public abstract class PersistingMekanismDatagenCache {
    @WrapOperation(method = "run", at = @At(value = "NEW", target = "net/minecraft/data/HashCache"))
    public HashCache newHashCache(Path rootDir, Collection<String> providerIds, WorldVersion version, Operation<HashCache> original){
        HashCache constructed = original.call(rootDir, providerIds, version);
        PersistingDisabledProvidersProvider.captureGlobalCache(constructed);
        return constructed;
    }
}
