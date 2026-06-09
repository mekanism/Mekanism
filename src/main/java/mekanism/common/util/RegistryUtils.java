package mekanism.common.util;

import java.util.Optional;
import mekanism.api.SerializationConstants;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public class RegistryUtils {

    private RegistryUtils() {
    }

    public static <R> String getNameForReporting(Registry<R> registry, R element) {
        //Based off of BlockEntity#getNameForReporting
        return registry.getKey(element) + " // " + element.getClass().getCanonicalName();
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
        if (nbt == null) {
            return Optional.empty();
        }
        return nbt.getString(SerializationConstants.ID)
              .map(Identifier::tryParse)
              .flatMap(registry::get);
    }

    public static String getPath(Block element) {
        return BuiltInRegistries.BLOCK.getKey(element).getPath();
    }

    public static <TYPE> Identifier getName(Holder<TYPE> element, DefaultedRegistry<TYPE> registry) {
        ResourceKey<?> key = element.getKey();
        if (key == null) {
            return registry.getKey(element.value());
        }
        return key.identifier();
    }

    @Nullable
    public static Identifier getName(Holder<?> element) {
        ResourceKey<?> key = element.getKey();
        return key == null ? null : key.identifier();
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Identifier getNameGeneric(Object element) {
        if (element instanceof Holder<?> holder) {
            //If we have a holder, just redirect to trying to look up the name with it
            // As this method is mostly a fallback, we don't care if it fails to find a name if someone has a registry of holders for some reason
            return getName(holder);
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            Identifier name = ((Registry) registry).getKeyOrNull(element);
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    public static Identifier synthetic(Identifier id, String prefix, String namespace) {
        return synthetic(Identifier.fromNamespaceAndPath(namespace, id.toString().replace(':', '_')), prefix);
    }

    /* Moved from RecipeViewerUtils, but unused?
    public static Identifier synthetic(String id, String prefix, String namespace) {
        if (id.equals("[unregistered]")) {
            return synthetic(Identifier.fromNamespaceAndPath(namespace, "_unregistered_sad_face_"), prefix);
        }
        return synthetic(Identifier.fromNamespaceAndPath(namespace, id.replace(':', '_')), prefix);
    }*/

    public static Identifier synthetic(ResourceKey<Recipe<?>> key, String prefix) {
        return synthetic(key.identifier(), prefix);
    }

    public static ResourceKey<Recipe<?>> syntheticKey(ResourceKey<Recipe<?>> key, String prefix) {
        return ResourceKey.create(key.registryKey(), synthetic(key.identifier(), prefix));
    }

    public static Identifier synthetic(Identifier id, String prefix) {
        return id.withPrefix("/" + prefix + "/");
    }
}