/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;

public interface IFakePlayerProvider {
    /**
     * Returns the generic buildcraft fake player. Note that you shouldn't use this anymore, as you should store the
     * UUID of the real player who created the block or entity that calls this.
     */
    @Deprecated
    FakePlayer getBuildCraftPlayer(WorldServer world);

    /**
     * @param world
     * @param profile The owner's profile.
     * @return A fake player that can be used IN THE CURRENT METHOD CONTEXT ONLY! This will cause problems if this
     * player is left around as it holds a reference to the world object.
     */
    FakePlayer getFakePlayer(WorldServer world, GameProfile profile);

    /**
     * @param world
     * @param profile The owner's profile.
     * @param pos
     * @return A fake player that can be used IN THE CURRENT METHOD CONTEXT ONLY! This will cause problems if this
     * player is left around as it holds a reference to the world object.
     */
    FakePlayer getFakePlayer(WorldServer world, GameProfile profile, BlockPos pos);
}
