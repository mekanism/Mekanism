package mekanism.common.network.to_server;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRemoveModule(BlockPos pos, ModuleData<?> moduleType) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("remove_module");

    public PacketRemoveModule(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readById(MekanismAPI.MODULE_REGISTRY));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        context.player().ifPresent(player -> {
            TileEntityModificationStation tile = WorldUtils.getTileEntity(TileEntityModificationStation.class, player.level(), pos);
            if (tile != null) {
                tile.removeModule(player, moduleType);
            }
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeId(MekanismAPI.MODULE_REGISTRY, moduleType);
    }
}
