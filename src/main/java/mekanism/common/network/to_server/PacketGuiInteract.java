package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/// Used for informing the server that an action happened in a tile GUI
public record PacketGuiInteract(GuiInteraction interaction, BlockPos tilePosition, int extra) implements IMekanismPacket {

    public static final Type<PacketGuiInteract> TYPE = new Type<>(Mekanism.rl("gui_interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGuiInteract> STREAM_CODEC = StreamCodec.composite(
          GuiInteraction.STREAM_CODEC, packet -> packet.interaction,
          BlockPos.STREAM_CODEC, packet -> packet.tilePosition,
          //TODO - 1.18?: Eventually we may want to try to make some form of this that can compact negatives better as well
          ByteBufCodecs.VAR_INT, packet -> packet.extra,
          PacketGuiInteract::new
    );

    public PacketGuiInteract(GuiInteraction interaction, BlockEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockEntity tile, int extra) {
        this(interaction, tile.getBlockPos(), extra);
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    @Override
    public Type<PacketGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
        if (tile != null) {
            interaction.consume(tile, player, extra);
        }
    }

    public enum GuiInteraction {//TODO: Cleanup this enum/the elements in it as it is rather disorganized order wise currently
        CONTAINER_STOP_TRACKING((_, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {
                container.stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_EJECTOR((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container && tile instanceof ISideConfiguration sideConfig) {
                container.startTrackingServer(extra, sideConfig.getEjector());
            }
        }),
        CONTAINER_TRACK_SIDE_CONFIG((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container && tile instanceof ISideConfiguration sideConfig) {
                container.startTrackingServer(extra, sideConfig.getConfig());
            }
        }),
        CONTAINER_TRACK_UPGRADES((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {//tile instanceof IUpgradeTile
                container.startTrackingServer(extra, tile.getComponent());
            }
        }),
        QIO_REDSTONE_ADAPTER_COUNT((tile, _, extra) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.handleCountChange(extra);
            }
        }),
        QIO_REDSTONE_ADAPTER_FUZZY((tile, _, _) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.toggleFuzzyMode();
            }
        }),
        QIO_TOGGLE_IMPORT_WITHOUT_FILTER((tile, _, _) -> {
            if (tile instanceof TileEntityQIOImporter importer) {
                importer.toggleImportWithoutFilter();
            }
        }),
        QIO_TOGGLE_EXPORT_WITHOUT_FILTER((tile, _, _) -> {
            if (tile instanceof TileEntityQIOExporter exporter) {
                exporter.toggleExportWithoutFilter();
            }
        }),
        AUTO_SORT_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityFactory<?> factory) {
                factory.toggleSorting();
            }
        }),
        TARGET_DIRECTION_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityQIODashboard dashboard) {
                dashboard.toggleShiftClickDirection();
            }
        }),
        DUMP_BUTTON((tile, _, _) -> {
            if (tile instanceof IHasDumpButton hasDumpButton) {
                hasDumpButton.dump();
            }
        }),
        GAS_MODE_BUTTON((tile, _, extra) -> {
            if (tile instanceof IHasGasMode hasGasMode) {
                hasGasMode.nextMode(extra);
            }
        }),

        AUTO_EJECT_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleAutoEject();
            } else if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.toggleAutoEject();
            }
        }),
        AUTO_PULL_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleAutoPull();
            }
        }),
        INVERSE_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleInverse();
            } else if (tile instanceof TileEntityQIORedstoneAdapter adapter) {
                adapter.invertSignal();
            }
        }),
        INVERSE_REQUIRES_REPLACEMENT_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleInverseRequiresReplacement();
            }
        }),
        RESET_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.reset();
            }
        }),
        SILK_TOUCH_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleSilkTouch();
            }
        }),
        START_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.start();
            }
        }),
        STOP_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.stop();
            }
        }),
        SET_RADIUS((tile, _, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setRadiusFromPacket(extra);
            }
        }),
        SET_MIN_Y((tile, _, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setMinYFromPacket(extra);
            }
        }),
        SET_MAX_Y((tile, _, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setMaxYFromPacket(extra);
            }
        }),

        MOVE_FILTER_UP((tile, _, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveUp(extra);
            }
        }),
        MOVE_FILTER_DOWN((tile, _, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveDown(extra);
            }
        }),
        MOVE_FILTER_TO_TOP((tile, _, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveToTop(extra);
            }
        }),
        MOVE_FILTER_TO_BOTTOM((tile, _, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveToBottom(extra);
            }
        }),
        TOGGLE_FILTER_STATE((tile, _, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder) {
                filterHolder.getFilterManager().toggleState(extra);
            }
        }),

        REMOVE_UPGRADE((tile, _, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.BY_ID.apply(extra), false);
            }
        }),
        REMOVE_ALL_UPGRADE((tile, _, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.BY_ID.apply(extra), true);
            }
        }),

        NEXT_SECURITY_MODE((tile, player, _) -> {
            if (tile.getLevel() != null) {
                SecurityUtils.get().incrementSecurityMode(player, IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile), null);
            }
        }),
        PREVIOUS_SECURITY_MODE((tile, player, _) -> {
            if (tile.getLevel() != null) {
                SecurityUtils.get().decrementSecurityMode(player, IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile), null);
            }
        }),

        SECURITY_DESK_MODE((tile, _, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.setSecurityDeskMode(SecurityMode.BY_ID.apply(extra));
            }
        }),

        NEXT_MODE((tile, _, _) -> {
            if (tile instanceof IHasMode hasMode) {
                hasMode.nextMode();
            }
        }),
        PREVIOUS_MODE((tile, _, _) -> {
            if (tile instanceof IHasMode hasMode) {
                hasMode.previousMode();
            }
        }),
        NEXT_REDSTONE_CONTROL((tile, _, _) -> tile.setControlType(tile.getControlType().getNext(tile::supportsMode))),
        PREVIOUS_REDSTONE_CONTROL((tile, _, _) -> tile.setControlType(tile.getControlType().getPrevious(tile::supportsMode))),
        ENCODE_FORMULA((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.encodeFormula();
            }
        }),
        STOCK_CONTROL_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.toggleStockControl();
            }
        }),
        CRAFT_SINGLE((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.craftSingle(tile.getLevel());
            }
        }),
        CRAFT_ALL((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.craftAll(tile.getLevel());
            }
        }),
        EMPTY_GRID((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.emptyGrid(tile.getLevel());
            }
        }),
        FILL_GRID((tile, _, _) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.fillGrid(tile.getLevel());
            }
        }),

        STRICT_INPUT((tile, _, _) -> {
            if (tile instanceof ISideConfiguration sideConfiguration) {
                TileComponentEjector ejector = sideConfiguration.getEjector();
                ejector.setStrictInput(!ejector.hasStrictInput());
            }
        }),

        ROUND_ROBIN_BUTTON((tile, _, _) -> {
            if (tile instanceof IAdvancedTransportEjector sorter) {
                sorter.toggleRoundRobin();
            }
        }),
        SINGLE_ITEM_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.toggleSingleItem();
            }
        }),
        CHANGE_COLOR((tile, _, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.changeColor(TransporterUtils.readColor(extra));
            }
        }),

        OVERRIDE_BUTTON((tile, _, _) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.toggleOverride();
            }
        }),
        REMOVE_TRUSTED((tile, _, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.removeTrusted(extra);
            }
        }),

        SET_TIME((tile, _, extra) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setDelay(extra);
            }
        }),
        MIN_THRESHOLD((tile, _, extra) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setMinThresholdFromPacket(extra);
            }
        }),
        MAX_THRESHOLD((tile, _, extra) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setMaxThresholdFromPacket(extra);
            }
        }),
        ENERGY_USAGE((tile, _, extra) -> {
            if (tile instanceof TileEntityResistiveHeater heater) {
                heater.setEnergyUsageFromPacket(extra);
            }
        }),

        TOGGLE_CHUNKLOAD((tile, _, extra) -> {
            if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
                stabilizer.toggleChunkLoadingAt(extra / TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER, extra % TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER);
            }
        }),
        ENABLE_RADIUS_CHUNKLOAD((tile, _, extra) -> {
            if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
                stabilizer.adjustChunkLoadingRadius(extra, true);
            }
        }),
        DISABLE_RADIUS_CHUNKLOAD((tile, _, extra) -> {
            if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
                stabilizer.adjustChunkLoadingRadius(extra, false);
            }
        });

        public static final IntFunction<GuiInteraction> BY_ID = ByIdMap.continuous(GuiInteraction::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiInteraction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiInteraction::ordinal);

        private final TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile;

        GuiInteraction(TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, int extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}