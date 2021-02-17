package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketClearRecipeCache implements IMekanismPacket {

    @Override
    public void handle(NetworkEvent.Context context) {
        MekanismRecipeType.clearCache();
    }

    @Override
    public void encode(PacketBuffer buffer) {
    }

    public static PacketClearRecipeCache decode(PacketBuffer buffer) {
        return new PacketClearRecipeCache();
    }
}