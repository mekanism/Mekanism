package mekanism.common.network.to_server.button;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketTileButtonPress(ClickedTileButton buttonClicked, BlockPos pos) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketTileButtonPress> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("tile_button"));
    public static final StreamCodec<ByteBuf, PacketTileButtonPress> STREAM_CODEC = StreamCodec.composite(
          ClickedTileButton.STREAM_CODEC, PacketTileButtonPress::buttonClicked,
          BlockPos.STREAM_CODEC, PacketTileButtonPress::pos,
          PacketTileButtonPress::new
    );

    public PacketTileButtonPress(ClickedTileButton buttonClicked, BlockEntity tile) {
        this(buttonClicked, tile.getBlockPos());
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketTileButtonPress> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), pos);
        MenuProvider provider = buttonClicked.getProvider(tile);
        if (provider != null) {
            player.openMenu(provider, buf -> {
                buf.writeBlockPos(pos);
                buttonClicked.encodeExtraData(buf, tile);
            });
        }
    }

    public enum ClickedTileButton {
        BACK_BUTTON(tile -> {
            //Special handling to basically reset to the tiles default gui container
            AttributeGui attributeGui = Attribute.get(tile.getBlockHolder(), AttributeGui.class);
            if (attributeGui != null) {
                return attributeGui.getProvider(tile, false);
            }
            return null;
        }, (buffer, tile) -> {
            //Note: This should always be true, as otherwise we wouldn't have a provider at the various call sites
            if (tile.hasGui()) {
                //Mirror the logic from TileEntityMekanism#openGui for what data we write so that we properly reinitialize the initial GUI
                tile.encodeExtraContainerData(buffer);
            }
        }),
        QIO_FREQUENCY_SELECT(tile -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, tile)),
        DIGITAL_MINER_CONFIG(tile -> MekanismContainerTypes.DIGITAL_MINER_CONFIG.getProvider(MekanismLang.MINER_CONFIG, tile)),

        TAB_MAIN(tile -> {
            if (tile instanceof TileEntityInductionCasing) {
                return MekanismContainerTypes.INDUCTION_MATRIX.getProvider(MekanismLang.MATRIX, tile);
            } else if (tile instanceof TileEntityBoilerCasing) {
                return MekanismContainerTypes.THERMOELECTRIC_BOILER.getProvider(MekanismLang.BOILER, tile);
            }
            return null;
        }),
        TAB_STATS(tile -> {
            if (tile instanceof TileEntityInductionCasing) {
                return MekanismContainerTypes.MATRIX_STATS.getProvider(MekanismLang.MATRIX_STATS, tile);
            } else if (tile instanceof TileEntityBoilerCasing) {
                return MekanismContainerTypes.BOILER_STATS.getProvider(MekanismLang.BOILER_STATS, tile);
            }
            return null;
        });

        public static final IntFunction<ClickedTileButton> BY_ID = ByIdMap.continuous(ClickedTileButton::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClickedTileButton> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClickedTileButton::ordinal);

        private final Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile;
        @Nullable
        private final BiConsumer<RegistryFriendlyByteBuf, TileEntityMekanism> extraEncodingData;

        ClickedTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile) {
            this(providerFromTile, null);
        }

        ClickedTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile,
              @Nullable BiConsumer<RegistryFriendlyByteBuf, TileEntityMekanism> extraEncodingData) {
            this.providerFromTile = providerFromTile;
            this.extraEncodingData = extraEncodingData;
        }

        @Nullable
        @Contract("null -> null")
        public MenuProvider getProvider(@Nullable TileEntityMekanism tile) {
            return tile == null ? null : providerFromTile.apply(tile);
        }

        private void encodeExtraData(RegistryFriendlyByteBuf buffer, TileEntityMekanism tile) {
            if (extraEncodingData != null) {
                extraEncodingData.accept(buffer, tile);
            }
        }
    }
}