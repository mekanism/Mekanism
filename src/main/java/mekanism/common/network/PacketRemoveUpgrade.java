package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.base.TileEntityMekanism;
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
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            if (tileEntity instanceof IUpgradeTile && tileEntity instanceof TileEntityMekanism) {
                IUpgradeTile upgradeTile = (IUpgradeTile) tileEntity;
                if (upgradeTile.getComponent().getUpgrades(message.upgradeType) > 0) {
                    if (player.inventory.addItemStackToInventory(message.upgradeType.getStack())) {
                        upgradeTile.getComponent().removeUpgrade(message.upgradeType);
                    }
                }
            }
        }, player);
    }

    public static void encode(PacketRemoveUpgrade pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeEnumValue(pkt.upgradeType);
    }

    public static PacketRemoveUpgrade decode(PacketBuffer buf) {
        return new PacketRemoveUpgrade(Coord4D.read(buf), buf.readEnumValue(Upgrade.class));
    }
}