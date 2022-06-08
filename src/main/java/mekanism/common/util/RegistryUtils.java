package mekanism.common.util;

import javax.annotation.Nullable;
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

public class RegistryUtils {

    private RegistryUtils() {
    }

    public static ResourceLocation getName(MenuType<?> element) {
        return getName(ForgeRegistries.CONTAINERS, element);
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
        return getName(ForgeRegistries.BLOCK_ENTITIES, element);
    }

    public static ResourceLocation getName(EntityType<?> element) {
        return getName(ForgeRegistries.ENTITIES, element);
    }

    public static ResourceLocation getName(RecipeSerializer<?> element) {
        return getName(ForgeRegistries.RECIPE_SERIALIZERS, element);
    }

    private static <T> ResourceLocation getName(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ResourceLocation getName(Object element) {
        //TODO - 1.19: Re-evaluate this
        for (Registry<?> registry : Registry.REGISTRY) {
            ResourceLocation registryName = ((Registry) registry).getKey(element);
            if (registryName != null) {
                return registryName;
            }
        }
        for (Registry<?> registry : BuiltinRegistries.REGISTRY) {
            ResourceLocation registryName = ((Registry) registry).getKey(element);
            if (registryName != null) {
                return registryName;
            }
        }
        return null;
    }
}