package mekanism.common.lib.multiblock;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

public record MultiblockType<DATA extends MultiblockData>(
      Identifier id,
      Supplier<MultiblockCache<DATA>> cacheSupplier,
      Supplier<IStructureValidator<DATA>> validatorSupplier,
      Supplier<Holder<AttachmentType<MultiblockManager<DATA>>>> attachmentSupplier
) {

    /// Note: It is important that callers also call [MultiblockManager#trackCache(UUID, MultiblockCache)] after initializing any data the cache might require.
    public MultiblockCache<DATA> createCache() {
        return cacheSupplier.get();
    }

    public IStructureValidator<DATA> createValidator() {
        return validatorSupplier.get();
    }

    public AttachmentType<MultiblockManager<DATA>> attachment() {
        return Objects.requireNonNull(attachmentSupplier.get().value());
    }
}
