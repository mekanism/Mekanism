package mekanism.common.network.to_server;

import java.util.function.BiConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

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
        Player player = context.getSender();
        if (player != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                interaction.consume(tile, value);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(tilePosition);
        value.writeToBuffer(buffer);
    }

    public static PacketGuiSetEnergy decode(FriendlyByteBuf buffer) {
        return new PacketGuiSetEnergy(buffer.readEnum(GuiEnergyValue.class), buffer.readBlockPos(), FloatingLong.readFromBuffer(buffer));
    }

    public enum GuiEnergyValue {
        MIN_THRESHOLD((tile, value) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setMinThresholdFromPacket(value);
            }
        }),
        MAX_THRESHOLD((tile, value) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setMaxThresholdFromPacket(value);
            }
        }),
        ENERGY_USAGE((tile, value) -> {
            if (tile instanceof TileEntityResistiveHeater heater) {
                heater.setEnergyUsageFromPacket(value);
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