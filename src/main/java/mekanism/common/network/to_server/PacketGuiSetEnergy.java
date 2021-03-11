package mekanism.common.network.to_server;

import java.util.function.BiConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketGuiSetEnergy implements IMekanismPacket {

    private final GuiEnergyValue interaction;
    private final BlockPos tilePosition;
    private final FloatingLong value;

    public PacketGuiSetEnergy(GuiEnergyValue interaction, BlockPos tilePosition, FloatingLong value) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.value = value;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                interaction.consume(tile, value);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(tilePosition);
        value.writeToBuffer(buffer);
    }

    public static PacketGuiSetEnergy decode(PacketBuffer buffer) {
        return new PacketGuiSetEnergy(buffer.readEnum(GuiEnergyValue.class), buffer.readBlockPos(), FloatingLong.readFromBuffer(buffer));
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