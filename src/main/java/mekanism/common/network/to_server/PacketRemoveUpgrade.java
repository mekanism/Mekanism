package mekanism.common.network.to_server;

import mekanism.api.upgrade.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRemoveUpgrade(BlockPos pos, Holder<Upgrade> upgradeType, boolean removeAll) implements IMekanismPacket {

    public static final Type<PacketRemoveUpgrade> TYPE = new Type<>(Mekanism.rl("remove_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRemoveUpgrade> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketRemoveUpgrade::pos,
          Upgrade.STREAM_CODEC, PacketRemoveUpgrade::upgradeType,
          ByteBufCodecs.BOOL, PacketRemoveUpgrade::removeAll,
          PacketRemoveUpgrade::new
    );

    @Override
    public Type<PacketRemoveUpgrade> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrades()) {
            TileComponentUpgrade component = upgradeTile.getComponent();
            if (component != null) {//Should never be null here
                component.removeUpgrade(level.registryAccess(), upgradeType, removeAll);
            }
        }
    }
}
