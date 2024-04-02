package mekanism.common.network.to_server;

import java.util.function.BiConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketGuiSetEnergy implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("set_energy");

    private final GuiEnergyValue interaction;
    private final BlockPos pos;
    private final FloatingLong value;

    public PacketGuiSetEnergy(FriendlyByteBuf buffer) {
        this(buffer.readEnum(GuiEnergyValue.class), buffer.readBlockPos(), FloatingLong.readFromBuffer(buffer));
    }

    public PacketGuiSetEnergy(GuiEnergyValue interaction, BlockPos pos, FloatingLong value) {
        this.interaction = interaction;
        this.pos = pos;
        this.value = value;
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        TileEntityMekanism tile = PacketUtils.blockEntity(context, pos, TileEntityMekanism.class);
        if (tile != null) {
            interaction.consume(tile, value);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(pos);
        value.writeToBuffer(buffer);
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