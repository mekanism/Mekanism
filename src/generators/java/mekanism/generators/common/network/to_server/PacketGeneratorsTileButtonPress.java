package mekanism.generators.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketGeneratorsTileButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos pos) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketGeneratorsTileButtonPress> TYPE = new CustomPacketPayload.Type<>(MekanismGenerators.rl("tile_button"));
    public static final StreamCodec<ByteBuf, PacketGeneratorsTileButtonPress> STREAM_CODEC = StreamCodec.composite(
          ClickedGeneratorsTileButton.STREAM_CODEC, PacketGeneratorsTileButtonPress::buttonClicked,
          BlockPos.STREAM_CODEC, PacketGeneratorsTileButtonPress::pos,
          PacketGeneratorsTileButtonPress::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketGeneratorsTileButtonPress> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), pos);
        MenuProvider provider = buttonClicked.getProvider(tile);
        if (provider != null) {
            player.openMenu(provider, buf -> {
                buf.writeBlockPos(pos);
                buttonClicked.encodeExtraData(buf, tile);
            });
        }
    }

    public enum ClickedGeneratorsTileButton {
        TAB_MAIN(tile -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return GeneratorsContainerTypes.INDUSTRIAL_TURBINE.getProvider(GeneratorsLang.TURBINE, tile);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return GeneratorsContainerTypes.FISSION_REACTOR.getProvider(GeneratorsLang.FISSION_REACTOR, tile);
            }
            return null;
        }, (buffer, tile) -> {
            //Mirror the logic from TileEntityMekanism#openGui for what data we write so that we properly reinitialize the initial GUI
            //TODO: Is this correct? I believe it is, and it doesn't hurt anything currently as it effectively is a NO-OP for both these cases
            // but there is a chance this isn't exactly correct
            tile.encodeExtraContainerData(buffer);
        }),
        TAB_HEAT(tile -> GeneratorsContainerTypes.FUSION_REACTOR_HEAT.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_FUEL(tile -> GeneratorsContainerTypes.FUSION_REACTOR_FUEL.getProvider(GeneratorsLang.FUSION_REACTOR, tile)),
        TAB_STATS(tile -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return GeneratorsContainerTypes.TURBINE_STATS.getProvider(GeneratorsLang.TURBINE_STATS, tile);
            } else if (tile instanceof TileEntityFusionReactorController) {
                return GeneratorsContainerTypes.FUSION_REACTOR_STATS.getProvider(GeneratorsLang.FUSION_REACTOR, tile);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return GeneratorsContainerTypes.FISSION_REACTOR_STATS.getProvider(GeneratorsLang.FISSION_REACTOR_STATS, tile);
            }
            return null;
        });

        public static final IntFunction<ClickedGeneratorsTileButton> BY_ID = ByIdMap.continuous(ClickedGeneratorsTileButton::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClickedGeneratorsTileButton> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClickedGeneratorsTileButton::ordinal);

        private final Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile;
        @Nullable
        private final BiConsumer<RegistryFriendlyByteBuf, TileEntityMekanism> extraEncodingData;

        ClickedGeneratorsTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile) {
            this(providerFromTile, null);
        }

        ClickedGeneratorsTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile,
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