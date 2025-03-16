package mekanism.common.util;

import java.util.Optional;
import mekanism.api.SerializationConstants;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {

    private RegistryUtils() {
    }

    public static Holder<BlockEntityType<?>> getBEHolder(BlockEntityType<?> type) {
        Holder<BlockEntityType<?>> holder = type.builtInRegistryHolder();
        //I don't believe this can ever be null as it is always instantiated, but just in case the nullability annotation is valid... handle it
        if (holder == null) {
            return BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type);
        }
        return holder;
    }

    public static <R> Optional<Holder.Reference<R>> getHolderById(CompoundTag nbt, Registry<R> registry) {
        if (nbt != null && nbt.contains(SerializationConstants.ID, Tag.TAG_STRING)) {
            ResourceLocation name = ResourceLocation.tryParse(nbt.getString(SerializationConstants.ID));
            if (name != null) {
                return registry.getHolder(name);
            }
        }
        return Optional.empty();
    }

    public static String getPath(Block element) {
        return BuiltInRegistries.BLOCK.getKey(element).getPath();
    }

    public static <TYPE> ResourceLocation getName(Holder<TYPE> element, DefaultedRegistry<TYPE> registry) {
        ResourceKey<?> key = element.getKey();
        if (key == null) {
            return registry.getKey(element.value());
        }
        return key.location();
    }

    @Nullable
    public static ResourceLocation getName(Holder<?> element) {
        ResourceKey<?> key = element.getKey();
        return key == null ? null : key.location();
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ResourceLocation getNameGeneric(Object element) {
        if (element instanceof Holder<?> holder) {
            //If we have a holder, just redirect to trying to look up the name with it
            // As this method is mostly a fallback, we don't care if it fails to find a name if someone has a registry of holders for some reason
            return getName(holder);
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            ResourceLocation name = ((Registry) registry).getKeyOrNull(element);
            if (name != null) {
                return name;
            }
        }
        return null;
    }
}