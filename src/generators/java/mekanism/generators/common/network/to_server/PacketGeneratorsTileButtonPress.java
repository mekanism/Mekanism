package mekanism.generators.common.network.to_server;

import java.util.function.Function;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketGeneratorsTileButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos pos) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = MekanismGenerators.rl("tile_button");

    public PacketGeneratorsTileButtonPress(FriendlyByteBuf buffer) {
        this(buffer.readEnum(ClickedGeneratorsTileButton.class), buffer.readBlockPos());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
            //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), pos);
            if (tile != null) {
                player.openMenu(buttonClicked.getProvider(tile), pos);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(buttonClicked);
        buffer.writeBlockPos(pos);
    }

    public enum ClickedGeneratorsTileButton {
        TAB_MAIN(tile -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return GeneratorsContainerTypes.INDUSTRIAL_TURBINE.getProvider(GeneratorsLang.TURBINE, tile);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return GeneratorsContainerTypes.FISSION_REACTOR.getProvider(GeneratorsLang.FISSION_REACTOR, tile);
            }
            return null;
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

        private final Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile;

        ClickedGeneratorsTileButton(Function<TileEntityMekanism, @Nullable MenuProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        @Nullable
        public MenuProvider getProvider(TileEntityMekanism tile) {
            return providerFromTile.apply(tile);
        }
    }
}