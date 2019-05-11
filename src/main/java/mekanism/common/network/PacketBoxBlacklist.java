package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.util.BlockInfo;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketBoxBlacklist implements IMessageHandler<BoxBlacklistMessage, IMessage> {

    @Override
    public IMessage onMessage(BoxBlacklistMessage message, MessageContext context) {
        return null;
    }

    public static class BoxBlacklistMessage implements IMessage {

        public BoxBlacklistMessage() {
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            Set<BlockInfo> boxIgnore = MekanismAPI.getBoxIgnore();
            dataStream.writeInt(boxIgnore.size());
            for (BlockInfo info : boxIgnore) {
                dataStream.writeInt(Block.getIdFromBlock(info.block));
                dataStream.writeInt(info.meta);
            }
            Set<String> boxModIgnore = MekanismAPI.getBoxModIgnore();
            dataStream.writeInt(boxModIgnore.size());
            for (String modid : boxModIgnore) {
                ByteBufUtils.writeUTF8String(dataStream, modid);
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            MekanismAPI.getBoxIgnore().clear();
            int amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                MekanismAPI.addBoxBlacklist(Block.getBlockById(dataStream.readInt()), dataStream.readInt());
            }
            int amountMods = dataStream.readInt();
            for (int i = 0; i < amountMods; i++) {
                MekanismAPI.addBoxBlacklistMod(ByteBufUtils.readUTF8String(dataStream));
            }
            Mekanism.logger.info("Received Cardboard Box blacklist entries from server (" + amount + " explicit blocks, " + amountMods + " mod wildcards)");
        }
    }
}