/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class BuildCraftAPI {
    public static IFakePlayerProvider fakePlayerProvider;

    public static final Set<Block> softBlocks = Sets.newHashSet();
    public static final HashMap<String, IWorldProperty> worldProperties = Maps.newHashMap();

    /** Deactivate constructor */
    private BuildCraftAPI() {}

    public static String getVersion() {
        ModContainer container = Loader.instance().getIndexedModList().get("buildcraftlib");
        if (container != null) {
            return container.getDisplayVersion();
        }
        return "UNKNOWN VERSION";
    }

    public static IWorldProperty getWorldProperty(String name) {
        return worldProperties.get(name);
    }

    public static void registerWorldProperty(String name, IWorldProperty property) {
        if (worldProperties.containsKey(name)) {
            BCLog.logger.warn("The WorldProperty key '" + name + "' is being overridden with " + property.getClass().getSimpleName() + "!");
        }
        worldProperties.put(name, property);
    }

    public static boolean isSoftBlock(World world, BlockPos pos) {
        return worldProperties.get("soft").get(world, pos);
    }

    public static ResourceLocation nameToResourceLocation(String name) {
        if (name.indexOf(':') > 0) return new ResourceLocation(name);
        ModContainer modContainer = Loader.instance().activeModContainer();
        if (modContainer == null) {
            throw new IllegalStateException("Illegal recipe name " + name + ". Provide domain id to register it correctly.");
        }
        return new ResourceLocation(modContainer.getModId(), name);
    }
}
