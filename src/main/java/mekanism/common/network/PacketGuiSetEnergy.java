package mekanism.common.network;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiSetEnergy {

    private final GuiEnergyValue interaction;
    private final BlockPos tilePosition;
    private final FloatingLong value;

    public PacketGuiSetEnergy(GuiEnergyValue interaction, BlockPos tilePosition, FloatingLong value) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.value = value;
    }

    public static void handle(PacketGuiSetEnergy message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
            if (tile != null) {
                message.interaction.consume(tile, message.value);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiSetEnergy pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.interaction);
        buf.writeBlockPos(pkt.tilePosition);
        pkt.value.writeToBuffer(buf);
    }

    public static PacketGuiSetEnergy decode(PacketBuffer buf) {
        return new PacketGuiSetEnergy(buf.readEnumValue(GuiEnergyValue.class), buf.readBlockPos(), FloatingLong.readFromBuffer(buf));
    }

    public enum GuiEnergyValue {
        MIN_THRESHOLD((tile, value) -> {
            if (tile instanceof TileEntityLaserAmplifier) {
                ((TileEntityLaserAmplifier) tile).setMinThresholdFromPacket(value);
            }
        }),
        MAX_THRESHOLD((tile, value) -> {
            if (tile instanceof TileEntityLaserAmplifier) {
                ((TileEntityLaserAmplifier) tile).setMaxThresholdFromPacket(value);
            }
        }),
        ENERGY_USAGE((tile, value) -> {
            if (tile instanceof TileEntityResistiveHeater) {
                ((TileEntityResistiveHeater) tile).setEnergyUsageFromPacket(value);
            }
        });

        private final BiConsumer<TileEntityMekanism, FloatingLong> consumerForTile;

        GuiEnergyValue(BiConsumer<TileEntityMekanism, FloatingLong> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, FloatingLong value) {
            consumerForTile.accept(tile, value);
        }
    }
}