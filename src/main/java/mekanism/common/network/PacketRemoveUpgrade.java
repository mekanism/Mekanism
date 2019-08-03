package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveUpgrade implements IMessageHandler<RemoveUpgradeMessage, IMessage> {

    @Override
    public IMessage onMessage(RemoveUpgradeMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            if (tileEntity instanceof IUpgradeTile && tileEntity instanceof TileEntityMekanism) {
                IUpgradeTile upgradeTile = (IUpgradeTile) tileEntity;
                Upgrade upgrade = Upgrade.values()[message.upgradeType];
                if (upgradeTile.getComponent().getUpgrades(upgrade) > 0) {
                    if (player.inventory.addItemStackToInventory(upgrade.getStack())) {
                        upgradeTile.getComponent().removeUpgrade(upgrade);
                    }
                }
            }
        }, player);
        return null;
    }

    public static class RemoveUpgradeMessage implements IMessage {

        public Coord4D coord4D;

        public int upgradeType;

        public RemoveUpgradeMessage() {
        }

        public RemoveUpgradeMessage(Coord4D coord, int type) {
            coord4D = coord;
            upgradeType = type;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            coord4D.write(dataStream);
            dataStream.writeInt(upgradeType);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            coord4D = Coord4D.read(dataStream);
            upgradeType = dataStream.readInt();
        }
    }
}