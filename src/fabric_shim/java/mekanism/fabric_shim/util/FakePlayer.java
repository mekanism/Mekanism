package mekanism.fabric_shim.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;

/**
 * Stand-in for net.neoforged.neoforge.common.util.FakePlayer on top of Fabric API's fake player,
 * exposing the constructor surface Mekanism's MekFakePlayer extends.
 */
public class FakePlayer extends net.fabricmc.fabric.api.entity.FakePlayer {

    public FakePlayer(ServerLevel level, GameProfile profile) {
        super(level, profile);
    }
}
