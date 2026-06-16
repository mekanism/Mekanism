package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus.Internal;

/// Registers MultiblockTypes and their associated AttachmentTypes for the manager instance. Otherwise, should be used much like a normal deferred registry.
///
/// Ensure [#register] is called during mod init
public class MekanismMultiblockRegistry {

    private static final List<MultiblockType<?>> ALL_TYPES_LIST = new ArrayList<>();
    @Internal//only used for mekanism.common.lib.multiblock.MultiblockManager.endOfTickEvent
    public static final List<MultiblockType<?>> ALL_TYPES = Collections.unmodifiableList(ALL_TYPES_LIST);

    public final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mekanism.MODID);
    private final Map<Identifier, Supplier<? extends AttachmentType<? extends MultiblockManager<?>>>> idToAttachmentType = new HashMap<>();

    private final String modId;

    public MekanismMultiblockRegistry(String modId) {
        this.modId = modId;
    }

    @SuppressWarnings("unchecked")
    private <DATA extends MultiblockData> AttachmentType<MultiblockManager<DATA>> getById(Identifier id) {
        return (AttachmentType<MultiblockManager<DATA>>) Objects.requireNonNull(idToAttachmentType.get(id), "Not found").get();
    }

    public <DATA extends MultiblockData> MultiblockType<DATA> registerMultiblock(
          String multiblockType,
          Supplier<MultiblockCache<DATA>> cacheSupplier,
          Supplier<IStructureValidator<DATA>> validatorSupplier
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(modId, multiblockType);
        MultiblockType<DATA> mbType = new MultiblockType<>(id, cacheSupplier, validatorSupplier, this::getById);
        idToAttachmentType.put(id, ATTACHMENT_TYPES.register(
              "multiblock/" + id.getPath(),
              () -> AttachmentType.serializable(() -> new MultiblockManager<>(mbType)).build()
        ));
        ALL_TYPES_LIST.add(mbType);
        return mbType;
    }

    public void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
