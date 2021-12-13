package mekanism.generators.common.network.to_server;

import java.util.function.BiFunction;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public class PacketGeneratorsGuiButtonPress implements IMekanismPacket {

    private final ClickedGeneratorsTileButton tileButton;
    private final int extra;
    private final BlockPos tilePosition;

    public PacketGeneratorsGuiButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGeneratorsGuiButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos tilePosition, int extra) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null) {//If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                INamedContainerProvider provider = tileButton.getProvider(tile, extra);
                if (provider != null) {
                    //Ensure valid data
                    NetworkHooks.openGui(player, provider, buf -> {
                        buf.writeBlockPos(tilePosition);
                        buf.writeVarInt(extra);
                    });
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(tileButton);
        buffer.writeBlockPos(tilePosition);
        buffer.writeVarInt(extra);
    }

    public static PacketGeneratorsGuiButtonPress decode(PacketBuffer buffer) {
        return new PacketGeneratorsGuiButtonPress(buffer.readEnum(ClickedGeneratorsTileButton.class), buffer.readBlockPos(), buffer.readVarInt());
    }

    public enum ClickedGeneratorsTileButton {
        TAB_MAIN((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return GeneratorsContainerTypes.INDUSTRIAL_TURBINE.getProvider(GeneratorsLang.TURBINE, tile);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return GeneratorsContainerTypes.FISSION_REACTOR.getProvider(GeneratorsLang.FISSION_REACTOR, tile);
            }
            return null;
        }),
        TAB_HEAT((tile, extra) -> GeneratorsContainerTypes.FUSION_REACTOR_HEAT.getProvider(GeneratorsLang.HEAT_TAB, tile)),
        TAB_FUEL((tile, extra) -> GeneratorsContainerTypes.FUSION_REACTOR_FUEL.getProvider(GeneratorsLang.FUEL_TAB, tile)),
        TAB_STATS((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return GeneratorsContainerTypes.TURBINE_STATS.getProvider(GeneratorsLang.TURBINE_STATS, tile);
            } else if (tile instanceof TileEntityFusionReactorController) {
                return GeneratorsContainerTypes.FUSION_REACTOR_STATS.getProvider(GeneratorsLang.STATS_TAB, tile);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return GeneratorsContainerTypes.FISSION_REACTOR_STATS.getProvider(GeneratorsLang.STATS_TAB, tile);
            }
            return null;
        });

        private final BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile;

        ClickedGeneratorsTileButton(BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public INamedContainerProvider getProvider(TileEntityMekanism tile, int extra) {
            return providerFromTile.apply(tile, extra);
        }
    }
}