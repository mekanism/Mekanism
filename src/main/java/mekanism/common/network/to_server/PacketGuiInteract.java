package mekanism.common.network.to_server;

import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.SecurityMode;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGuiInteract implements IMekanismPacket {

    private final Type interactionType;

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
        this.interactionType = Type.ENTITY;
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
        this.interactionType = Type.INT;
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public PacketGuiInteract(GuiInteractionItem interaction, BlockEntity tile, ItemStack stack) {
        this(interaction, tile.getBlockPos(), stack);
    }

    public PacketGuiInteract(GuiInteractionItem interaction, BlockPos tilePosition, ItemStack stack) {
        this.interactionType = Type.ITEM;
        this.itemInteraction = interaction;
        this.tilePosition = tilePosition;
        this.extraItem = stack;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            if (interactionType == Type.ENTITY) {
                Entity entity = player.level.getEntity(entityID);
                if (entity != null) {
                    entityInteraction.consume(entity, player, extra);
                }
            } else {
                TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
                if (tile != null) {
                    if (interactionType == Type.INT) {
                        interaction.consume(tile, player, extra);
                    } else if (interactionType == Type.ITEM) {
                        itemInteraction.consume(tile, player, extraItem);
                    }
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(interactionType);
        switch (interactionType) {
            case ENTITY -> {
                buffer.writeEnum(entityInteraction);
                buffer.writeVarInt(entityID);
                buffer.writeVarInt(extra);
            }
            case INT -> {
                buffer.writeEnum(interaction);
                buffer.writeBlockPos(tilePosition);
                //TODO - 1.18?: Eventually we may want to try to make some form of this that can compact negatives better as well
                buffer.writeVarInt(extra);
            }
            case ITEM -> {
                buffer.writeEnum(itemInteraction);
                buffer.writeBlockPos(tilePosition);
                buffer.writeItem(extraItem);
            }
        }
    }

    public static PacketGuiInteract decode(FriendlyByteBuf buffer) {
        return switch (buffer.readEnum(Type.class)) {
            case ENTITY -> new PacketGuiInteract(buffer.readEnum(GuiInteractionEntity.class), buffer.readVarInt(), buffer.readVarInt());
            case INT -> new PacketGuiInteract(buffer.readEnum(GuiInteraction.class), buffer.readBlockPos(), buffer.readVarInt());
            case ITEM -> new PacketGuiInteract(buffer.readEnum(GuiInteractionItem.class), buffer.readBlockPos(), buffer.readItem());
        };
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
                container.startTracking(extra, sideConfig.getEjector());
            }
        }),
        CONTAINER_TRACK_SIDE_CONFIG((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container && tile instanceof ISideConfiguration sideConfig) {
                container.startTracking(extra, sideConfig.getConfig());
            }
        }),
        CONTAINER_TRACK_UPGRADES((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {//tile instanceof IUpgradeTile
                container.startTracking(extra, ((IUpgradeTile) tile).getComponent());
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
            if (tile instanceof IHasSortableFilters hasSortableFilters) {
                hasSortableFilters.moveUp(extra);
            }
        }),
        MOVE_FILTER_DOWN((tile, player, extra) -> {
            if (tile instanceof IHasSortableFilters hasSortableFilters) {
                hasSortableFilters.moveDown(extra);
            }
        }),

        REMOVE_UPGRADE((tile, player, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.byIndexStatic(extra), false);
            }
        }),
        REMOVE_ALL_UPGRADE((tile, player, extra) -> {
            if (tile.supportsUpgrades()) {
                tile.getComponent().removeUpgrade(Upgrade.byIndexStatic(extra), true);
            }
        }),

        NEXT_SECURITY_MODE((tile, player, extra) -> SecurityUtils.INSTANCE.incrementSecurityMode(player, tile)),

        SECURITY_DESK_MODE((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk desk) {
                desk.setSecurityDeskMode(SecurityMode.byIndexStatic(extra));
            }
        }),

        NEXT_MODE((tile, player, extra) -> {
            if (tile instanceof IHasMode hasMode) {
                hasMode.nextMode();
            }
        }),
        NEXT_REDSTONE_CONTROL((tile, player, extra) -> tile.setControlType(tile.getControlType().getNext(mode -> mode != RedstoneControl.PULSE || tile.canPulse()))),
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
        MOVE_ITEMS((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
                assemblicator.moveItems();
            }
        }),

        ROUND_ROBIN_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter sorter) {
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
        })

        ;

        private final TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile;

        GuiInteraction(TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, int extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }

    public enum GuiInteractionEntity {
        NEXT_SECURITY_MODE((entity, player, extra) -> SecurityUtils.INSTANCE.incrementSecurityMode(player, entity)),
        CONTAINER_STOP_TRACKING((entity, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {
                container.stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_SKIN_SELECT((entity, player, extra) -> {
            if (player.containerMenu instanceof MainRobitContainer container) {
                container.startTracking(extra, container);
            }
        }),
        ;

        private final TriConsumer<Entity, Player, Integer> consumerForEntity;

        GuiInteractionEntity(TriConsumer<Entity, Player, Integer> consumerForEntity) {
            this.consumerForEntity = consumerForEntity;
        }

        public void consume(Entity entity, Player player, int extra) {
            consumerForEntity.accept(entity, player, extra);
        }
    }

    private enum Type {
        ENTITY,
        ITEM,
        INT;
    }
}