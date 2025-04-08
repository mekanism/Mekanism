package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
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
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGuiInteract implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketGuiInteract> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("gui_interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGuiInteract> STREAM_CODEC = InteractionType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(packet -> packet.interactionType, type -> switch (type) {
              case ENTITY -> StreamCodec.composite(
                    GuiInteractionEntity.STREAM_CODEC, packet -> packet.entityInteraction,
                    ByteBufCodecs.VAR_INT, packet -> packet.entityID,
                    ByteBufCodecs.VAR_INT, packet -> packet.extra,
                    PacketGuiInteract::new
              );
              case INT -> StreamCodec.composite(
                    GuiInteraction.STREAM_CODEC, packet -> packet.interaction,
                    BlockPos.STREAM_CODEC, packet -> packet.tilePosition,
                    //TODO - 1.18?: Eventually we may want to try to make some form of this that can compact negatives better as well
                    ByteBufCodecs.VAR_INT, packet -> packet.extra,
                    PacketGuiInteract::new
              );
              case ITEM -> StreamCodec.composite(
                    GuiInteractionItem.STREAM_CODEC, packet -> packet.itemInteraction,
                    BlockPos.STREAM_CODEC, packet -> packet.tilePosition,
                    ItemStack.OPTIONAL_STREAM_CODEC, packet -> packet.extraItem,
                    PacketGuiInteract::new
              );
          });

    private final InteractionType interactionType;

    private GuiInteraction interaction;
    private GuiInteractionItem itemInteraction;
    private GuiInteractionEntity entityInteraction;
    private BlockPos tilePosition;
    private ItemStack extraItem;
    private int entityID;
    private int extra;

    public PacketGuiInteract(GuiInteractionEntity interaction, Entity entity) {
        this(interaction, entity, 0);
    }

    public PacketGuiInteract(GuiInteractionEntity interaction, Entity entity, int extra) {
        this(interaction, entity.getId(), extra);
    }

    public PacketGuiInteract(GuiInteractionEntity interaction, int entityID, int extra) {
        this.interactionType = InteractionType.ENTITY;
        this.entityInteraction = interaction;
        this.entityID = entityID;
        this.extra = extra;
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockEntity tile, int extra) {
        this(interaction, tile.getBlockPos(), extra);
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    public PacketGuiInteract(GuiInteraction interaction, BlockPos tilePosition, int extra) {
        this.interactionType = InteractionType.INT;
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public PacketGuiInteract(GuiInteractionItem interaction, BlockEntity tile, ItemStack stack) {
        this(interaction, tile.getBlockPos(), stack);
    }

    public PacketGuiInteract(GuiInteractionItem interaction, BlockPos tilePosition, ItemStack stack) {
        this.interactionType = InteractionType.ITEM;
        this.itemInteraction = interaction;
        this.tilePosition = tilePosition;
        this.extraItem = stack;
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (interactionType == InteractionType.ENTITY) {
            Entity entity = player.level().getEntity(entityID);
            if (entity != null) {
                entityInteraction.consume(entity, player, extra);
            }
        } else {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
            if (tile != null) {
                if (interactionType == InteractionType.INT) {
                    interaction.consume(tile, player, extra);
                } else if (interactionType == InteractionType.ITEM) {
                    itemInteraction.consume(tile, player, extraItem);
                }
            }
        }
    }

    public enum GuiInteractionItem {
        DIGITAL_MINER_INVERSE_REPLACE_ITEM((tile, player, stack) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setInverseReplaceTarget(stack.getItem());
            }
        }),
        QIO_REDSTONE_ADAPTER_STACK((tile, player, stack) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.handleStackChange(stack);
            }
        });

        public static final IntFunction<GuiInteractionItem> BY_ID = ByIdMap.continuous(GuiInteractionItem::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiInteractionItem> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiInteractionItem::ordinal);

        private final TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile;

        GuiInteractionItem(TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, ItemStack stack) {
            consumerForTile.accept(tile, player, stack);
        }
    }

    public enum GuiInteraction {//TODO: Cleanup this enum/the elements in it as it is rather disorganized order wise currently
        CONTAINER_STOP_TRACKING((tile, player, extra) -> {
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
        QIO_REDSTONE_ADAPTER_COUNT((tile, player, extra) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.handleCountChange(extra);
            }
        }),
        QIO_REDSTONE_ADAPTER_FUZZY((tile, player, extra) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.toggleFuzzyMode();
            }
        }),
        QIO_TOGGLE_IMPORT_WITHOUT_FILTER((tile, player, extra) -> {
            if (tile instanceof TileEntityQIOImporter importer) {
                importer.toggleImportWithoutFilter();
            }
        }),
        QIO_TOGGLE_EXPORT_WITHOUT_FILTER((tile, player, extra) -> {
            if (tile instanceof TileEntityQIOExporter exporter) {
                exporter.toggleExportWithoutFilter();
            }
        }),
        AUTO_SORT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityFactory<?> factory) {
                factory.toggleSorting();
            }
        }),
        TARGET_DIRECTION_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityQIODashboard dashboard) {
                dashboard.toggleShiftClickDirection();
            }
        }),
        DUMP_BUTTON((tile, player, extra) -> {
            if (tile instanceof IHasDumpButton hasDumpButton) {
                hasDumpButton.dump();
            }
        }),
        GAS_MODE_BUTTON((tile, player, extra) -> {
            if (tile instanceof IHasGasMode hasGasMode) {
                hasGasMode.nextMode(extra);
            }
        }),

        AUTO_EJECT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleAutoEject();
            } else if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.toggleAutoEject();
            }
        }),
        AUTO_PULL_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleAutoPull();
            }
        }),
        INVERSE_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleInverse();
            } else if (tile instanceof TileEntityQIORedstoneAdapter adapter) {
                adapter.invertSignal();
            }
        }),
        INVERSE_REQUIRES_REPLACEMENT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleInverseRequiresReplacement();
            }
        }),
        RESET_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.reset();
            }
        }),
        SILK_TOUCH_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.toggleSilkTouch();
            }
        }),
        START_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.start();
            }
        }),
        STOP_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.stop();
            }
        }),
        SET_RADIUS((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setRadiusFromPacket(extra);
            }
        }),
        SET_MIN_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setMinYFromPacket(extra);
            }
        }),
        SET_MAX_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setMaxYFromPacket(extra);
            }
        }),

        MOVE_FILTER_UP((tile, player, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveUp(extra);
            }
        }),
        MOVE_FILTER_DOWN((tile, player, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveDown(extra);
            }
        }),
        MOVE_FILTER_TO_TOP((tile, player, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveToTop(extra);
            }
        }),
        MOVE_FILTER_TO_BOTTOM((tile, player, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
                manager.moveToBottom(extra);
            }
        }),
        TOGGLE_FILTER_STATE((tile, player, extra) -> {
            if (tile instanceof ITileFilterHolder<?> filterHolder) {
                filterHolder.getFilterManager().toggleState(extra);
            }
        }),

        REMOVE_UPGRADE((tile, player, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.BY_ID.apply(extra), false);
            }
        }),
        REMOVE_ALL_UPGRADE((tile, player, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.BY_ID.apply(extra), true);
            }
        }),

        NEXT_SECURITY_MODE((tile, player, extra) -> {
            if (tile.getLevel() != null) {
                SecurityUtils.get().incrementSecurityMode(player, IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile));
            }
        }),
        PREVIOUS_SECURITY_MODE((tile, player, extra) -> {
            if (tile.getLevel() != null) {
                SecurityUtils.get().decrementSecurityMode(player, IBlockSecurityUtils.INSTANCE.securityCapability(tile.getLevel(), tile.getBlockPos(), tile));
            }
        }),

        SECURITY_DESK_MODE((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.setSecurityDeskMode(SecurityMode.BY_ID.apply(extra));
            }
        }),

        NEXT_MODE((tile, player, extra) -> {
            if (tile instanceof IHasMode hasMode) {
                hasMode.nextMode();
            }
        }),
        PREVIOUS_MODE((tile, player, extra) -> {
            if (tile instanceof IHasMode hasMode) {
                hasMode.previousMode();
            }
        }),
        NEXT_REDSTONE_CONTROL((tile, player, extra) -> tile.setControlType(tile.getControlType().getNext(tile::supportsMode))),
        PREVIOUS_REDSTONE_CONTROL((tile, player, extra) -> tile.setControlType(tile.getControlType().getPrevious(tile::supportsMode))),
        ENCODE_FORMULA((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.encodeFormula();
            }
        }),
        STOCK_CONTROL_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.toggleStockControl();
            }
        }),
        CRAFT_SINGLE((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.craftSingle();
            }
        }),
        CRAFT_ALL((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.craftAll();
            }
        }),
        EMPTY_GRID((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.emptyGrid();
            }
        }),
        FILL_GRID((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.fillGrid();
            }
        }),

        STRICT_INPUT((tile, player, extra) -> {
            if (tile instanceof ISideConfiguration sideConfiguration) {
                TileComponentEjector ejector = sideConfiguration.getEjector();
                ejector.setStrictInput(!ejector.hasStrictInput());
            }
        }),

        ROUND_ROBIN_BUTTON((tile, player, extra) -> {
            if (tile instanceof IAdvancedTransportEjector sorter) {
                sorter.toggleRoundRobin();
            }
        }),
        SINGLE_ITEM_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.toggleSingleItem();
            }
        }),
        CHANGE_COLOR((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter sorter) {
                sorter.changeColor(TransporterUtils.readColor(extra));
            }
        }),

        OVERRIDE_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.toggleOverride();
            }
        }),
        REMOVE_TRUSTED((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.removeTrusted(extra);
            }
        }),

        SET_TIME((tile, player, extra) -> {
            if (tile instanceof TileEntityLaserAmplifier amplifier) {
                amplifier.setDelay(extra);
            }
        }),

        TOGGLE_CHUNKLOAD((tile, player, extra) -> {
            if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
                stabilizer.toggleChunkLoadingAt(extra / TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER, extra % TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER);
            }
        }),
        ENABLE_RADIUS_CHUNKLOAD((tile, player, extra) -> {
            if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
                stabilizer.adjustChunkLoadingRadius(extra, true);
            }
        }),
        DISABLE_RADIUS_CHUNKLOAD((tile, player, extra) -> {
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

    public enum GuiInteractionEntity {
        NEXT_SECURITY_MODE((entity, player, extra) -> SecurityUtils.get().incrementSecurityMode(player, IEntitySecurityUtils.INSTANCE.securityCapability(entity))),
        PREVIOUS_SECURITY_MODE((entity, player, extra) -> SecurityUtils.get().decrementSecurityMode(player, IEntitySecurityUtils.INSTANCE.securityCapability(entity))),
        CONTAINER_STOP_TRACKING((entity, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {
                container.stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_SKIN_SELECT((entity, player, extra) -> {
            if (player.containerMenu instanceof MainRobitContainer container) {
                container.startTrackingServer(extra, container);
            }
        }),

        GO_HOME((entity, player, extra) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.goHome();
            }
        }),
        FOLLOW((entity, player, extra) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.setFollowing(!robit.getFollowing());
            }
        }),
        PICKUP_DROPS((entity, player, extra) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.setDropPickup(!robit.getDropPickup());
            }
        }),
        ;

        public static final IntFunction<GuiInteractionEntity> BY_ID = ByIdMap.continuous(GuiInteractionEntity::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiInteractionEntity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiInteractionEntity::ordinal);

        private final TriConsumer<Entity, Player, Integer> consumerForEntity;

        GuiInteractionEntity(TriConsumer<Entity, Player, Integer> consumerForEntity) {
            this.consumerForEntity = consumerForEntity;
        }

        public void consume(Entity entity, Player player, int extra) {
            consumerForEntity.accept(entity, player, extra);
        }
    }

    private enum InteractionType {
        ENTITY,
        ITEM,
        INT;

        public static final IntFunction<InteractionType> BY_ID = ByIdMap.continuous(InteractionType::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, InteractionType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InteractionType::ordinal);
    }
}