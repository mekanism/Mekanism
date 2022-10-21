package mekanism.common.util;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {

    private RegistryUtils() {
    }

    public static ResourceLocation getName(MenuType<?> element) {
        return getName(ForgeRegistries.MENU_TYPES, element);
    }

    public static ResourceLocation getName(ParticleType<?> element) {
        return getName(ForgeRegistries.PARTICLE_TYPES, element);
    }

    public static ResourceLocation getName(Item element) {
        return getName(ForgeRegistries.ITEMS, element);
    }

    public static String getPath(Item element) {
        return getName(element).getPath();
    }

    public static ResourceLocation getName(Block element) {
        return getName(ForgeRegistries.BLOCKS, element);
    }

    public static String getNamespace(Block element) {
        return getName(element).getNamespace();
    }

    public static String getPath(Block element) {
        return getName(element).getPath();
    }

    public static ResourceLocation getName(Fluid element) {
        return getName(ForgeRegistries.FLUIDS, element);
    }

    public static ResourceLocation getName(BlockEntityType<?> element) {
        return getName(ForgeRegistries.BLOCK_ENTITY_TYPES, element);
    }

    public static ResourceLocation getName(EntityType<?> element) {
        return getName(ForgeRegistries.ENTITY_TYPES, element);
    }

    public static ResourceLocation getName(RecipeSerializer<?> element) {
        return getName(ForgeRegistries.RECIPE_SERIALIZERS, element);
    }

    private static <T> ResourceLocation getName(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }

    @Nullable
    public static ResourceLocation getName(Object element) {
        ResourceLocation registryName = getName(Registry.REGISTRY, element);
        return registryName == null ? getName(BuiltinRegistries.REGISTRY, element) : registryName;
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResourceLocation getName(Registry<? extends Registry<?>> registries, Object element) {
        for (Registry<?> registry : registries) {
            ResourceLocation registryName = ((Registry) registry).getKey(element);
            if (registryName != null) {
                return registryName;
            }
        }
        return null;
    }
}