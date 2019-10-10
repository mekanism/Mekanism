package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketRemoveUpgrade {

    private Coord4D coord4D;
    private Upgrade upgradeType;

    public PacketRemoveUpgrade(Coord4D coord, Upgrade type) {
        coord4D = coord;
        upgradeType = type;
    }

    public static void handle(PacketRemoveUpgrade message, Supplier<Context> context) {
        PlayerEntity player = context.get().getSender();
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            if (tileEntity instanceof TileEntityMekanism) {
                TileEntityMekanism upgradeTile = (TileEntityMekanism) tileEntity;
                if (upgradeTile.supportsUpgrades() && upgradeTile.getComponent().getUpgrades(message.upgradeType) > 0) {
                    if (player.inventory.addItemStackToInventory(UpgradeUtils.getStack(message.upgradeType))) {
                        upgradeTile.getComponent().removeUpgrade(message.upgradeType);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketRemoveUpgrade pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeEnumValue(pkt.upgradeType);
    }

    public static PacketRemoveUpgrade decode(PacketBuffer buf) {
        return new PacketRemoveUpgrade(Coord4D.read(buf), buf.readEnumValue(Upgrade.class));
    }
}