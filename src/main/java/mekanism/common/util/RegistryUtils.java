package mekanism.common.util;

import java.util.Optional;
import mekanism.api.SerializationConstants;
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
        //I don't believe this can ever be null, but just in case the nullability annotation is valid... handle it
        if (holder == null) {
            return BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type);
        }
        return holder;
    }

    public static <R> Optional<Holder<R>> getHolderById(CompoundTag nbt, Registry<R> registry) {
        return Optional.ofNullable(nbt)
              .filter(tag -> tag.contains(SerializationConstants.ID, Tag.TAG_STRING))
              .map(tag -> tag.getString(SerializationConstants.ID))
              .map(ResourceLocation::tryParse)
              .flatMap(registry::getHolder);
    }

    public static String getPath(Block element) {
        return BuiltInRegistries.BLOCK.getKey(element).getPath();
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
            return getName(holder);
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            //Note: We have to use getResourceKey as getKey for defaulted registries returns the default key
            Optional<ResourceKey<?>> resourceKey = ((Registry) registry).getResourceKey(element);
            if (resourceKey.isPresent()) {
                return resourceKey.get().location();
            }
        }
        return null;
    }
}