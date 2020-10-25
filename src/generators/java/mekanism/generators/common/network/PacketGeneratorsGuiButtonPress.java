package mekanism.generators.common.network;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.container.FusionReactorFuelTabContainer;
import mekanism.generators.common.container.FusionReactorHeatTabContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public class PacketGeneratorsGuiButtonPress {

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

    public static void handle(PacketGeneratorsGuiButtonPress message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null) {//If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
                TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
                if (tile != null) {
                    INamedContainerProvider provider = message.tileButton.getProvider(tile, message.extra);
                    if (provider != null) {
                        //Ensure valid data
                        NetworkHooks.openGui(player, provider, buf -> {
                            buf.writeBlockPos(message.tilePosition);
                            buf.writeVarInt(message.extra);
                        });
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketGeneratorsGuiButtonPress pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.tileButton);
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeVarInt(pkt.extra);
    }

    public static PacketGeneratorsGuiButtonPress decode(PacketBuffer buf) {
        return new PacketGeneratorsGuiButtonPress(buf.readEnumValue(ClickedGeneratorsTileButton.class), buf.readBlockPos(), buf.readVarInt());
    }

    public enum ClickedGeneratorsTileButton {
        TAB_MAIN((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return new ContainerProvider(GeneratorsLang.TURBINE, (i, inv, player) -> new MekanismTileContainer<>(GeneratorsContainerTypes.INDUSTRIAL_TURBINE, i, inv, (TileEntityTurbineCasing) tile));
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return new ContainerProvider(GeneratorsLang.FISSION_REACTOR, (i, inv, player) -> new EmptyTileContainer<>(GeneratorsContainerTypes.FISSION_REACTOR, i, inv, (TileEntityFissionReactorCasing) tile));
            }
            return null;
        }),
        TAB_HEAT((tile, extra) -> {
            if (tile instanceof TileEntityFusionReactorController) {
                return new ContainerProvider(GeneratorsLang.HEAT_TAB, (i, inv, player) -> new FusionReactorHeatTabContainer(i, inv, (TileEntityFusionReactorController) tile));
            }
            return null;
        }),
        TAB_FUEL((tile, extra) -> {
            if (tile instanceof TileEntityFusionReactorController) {
                return new ContainerProvider(GeneratorsLang.FUEL_TAB, (i, inv, player) -> new FusionReactorFuelTabContainer(i, inv, (TileEntityFusionReactorController) tile));
            }
            return null;
        }),
        TAB_STATS((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return new ContainerProvider(GeneratorsLang.TURBINE_STATS, (i, inv, player) -> new EmptyTileContainer<>(GeneratorsContainerTypes.TURBINE_STATS, i, inv, (TileEntityTurbineCasing) tile));
            } else if (tile instanceof TileEntityFusionReactorController) {
                return new ContainerProvider(GeneratorsLang.STATS_TAB, (i, inv, player) -> new EmptyTileContainer<>(GeneratorsContainerTypes.FUSION_REACTOR_STATS, i, inv, (TileEntityFusionReactorController) tile));
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                return new ContainerProvider(GeneratorsLang.STATS_TAB, (i, inv, player) -> new EmptyTileContainer<>(GeneratorsContainerTypes.FISSION_REACTOR_STATS, i, inv, (TileEntityFissionReactorCasing) tile));
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