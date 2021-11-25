package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.network.IMekanismPacket;
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
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public PacketGuiInteract(GuiInteraction interaction, TileEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketGuiInteract(GuiInteraction interaction, TileEntity tile, int extra) {
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

    public PacketGuiInteract(GuiInteractionItem interaction, TileEntity tile, ItemStack stack) {
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
        PlayerEntity player = context.getSender();
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
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(interactionType);
        if (interactionType == Type.ENTITY) {
            buffer.writeEnum(entityInteraction);
            buffer.writeVarInt(entityID);
            buffer.writeVarInt(extra);
        } else if (interactionType == Type.INT) {
            buffer.writeEnum(interaction);
            buffer.writeBlockPos(tilePosition);
            buffer.writeVarInt(extra);
        } else if (interactionType == Type.ITEM) {
            buffer.writeEnum(itemInteraction);
            buffer.writeBlockPos(tilePosition);
            buffer.writeItem(extraItem);
        }
    }

    public static PacketGuiInteract decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        if (type == Type.ENTITY) {
            return new PacketGuiInteract(buffer.readEnum(GuiInteractionEntity.class), buffer.readVarInt(), buffer.readVarInt());
        } else if (type == Type.INT) {
            return new PacketGuiInteract(buffer.readEnum(GuiInteraction.class), buffer.readBlockPos(), buffer.readVarInt());
        } else if (type == Type.ITEM) {
            return new PacketGuiInteract(buffer.readEnum(GuiInteractionItem.class), buffer.readBlockPos(), buffer.readItem());
        }
        Mekanism.logger.error("Received malformed GUI interaction packet.");
        return null;
    }

    public enum GuiInteractionItem {
        DIGITAL_MINER_INVERSE_REPLACE_ITEM((tile, player, stack) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setInverseReplaceTarget(stack.getItem());
            }
        }),
        QIO_REDSTONE_ADAPTER_STACK((tile, player, stack) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter) {
                ((TileEntityQIORedstoneAdapter) tile).handleStackChange(stack);
            }
        });

        private final TriConsumer<TileEntityMekanism, PlayerEntity, ItemStack> consumerForTile;

        GuiInteractionItem(TriConsumer<TileEntityMekanism, PlayerEntity, ItemStack> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, PlayerEntity player, ItemStack stack) {
            consumerForTile.accept(tile, player, stack);
        }
    }

    public enum GuiInteraction {//TODO: Cleanup this enum/the elements in it as it is rather disorganized order wise currently
        CONTAINER_STOP_TRACKING((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer) {
                ((MekanismContainer) player.containerMenu).stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_EJECTOR((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer && tile instanceof ISideConfiguration) {
                ((MekanismContainer) player.containerMenu).startTracking(extra, ((ISideConfiguration) tile).getEjector());
            }
        }),
        CONTAINER_TRACK_SIDE_CONFIG((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer && tile instanceof ISideConfiguration) {
                ((MekanismContainer) player.containerMenu).startTracking(extra, ((ISideConfiguration) tile).getConfig());
            }
        }),
        CONTAINER_TRACK_UPGRADES((tile, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer) {//tile instanceof IUpgradeTile
                ((MekanismContainer) player.containerMenu).startTracking(extra, ((IUpgradeTile) tile).getComponent());
            }
        }),
        QIO_REDSTONE_ADAPTER_COUNT((tile, player, extra) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter) {
                ((TileEntityQIORedstoneAdapter) tile).handleCountChange(extra);
            }
        }),
        QIO_TOGGLE_IMPORT_WITHOUT_FILTER((tile, player, extra) -> {
            if (tile instanceof TileEntityQIOImporter) {
                ((TileEntityQIOImporter) tile).toggleImportWithoutFilter();
            }
        }),
        QIO_TOGGLE_EXPORT_WITHOUT_FILTER((tile, player, extra) -> {
            if (tile instanceof TileEntityQIOExporter) {
                ((TileEntityQIOExporter) tile).toggleExportWithoutFilter();
            }
        }),
        AUTO_SORT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityFactory) {
                ((TileEntityFactory<?>) tile).toggleSorting();
            }
        }),
        DUMP_BUTTON((tile, player, extra) -> {
            if (tile instanceof IHasDumpButton) {
                ((IHasDumpButton) tile).dump();
            }
        }),
        GAS_MODE_BUTTON((tile, player, extra) -> {
            if (tile instanceof IHasGasMode) {
                ((IHasGasMode) tile).nextMode(extra);
            }
        }),

        AUTO_EJECT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleAutoEject();
            } else if (tile instanceof TileEntityLogisticalSorter) {
                ((TileEntityLogisticalSorter) tile).toggleAutoEject();
            }
        }),
        AUTO_PULL_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleAutoPull();
            }
        }),
        INVERSE_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleInverse();
            }
        }),
        INVERSE_REQUIRES_REPLACEMENT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleInverseRequiresReplacement();
            }
        }),
        RESET_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).reset();
            }
        }),
        SILK_TOUCH_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleSilkTouch();
            }
        }),
        START_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).start();
            }
        }),
        STOP_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).stop();
            }
        }),
        SET_RADIUS((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setRadiusFromPacket(extra);
            }
        }),
        SET_MIN_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setMinYFromPacket(extra);
            }
        }),
        SET_MAX_Y((tile, player, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setMaxYFromPacket(extra);
            }
        }),

        MOVE_FILTER_UP((tile, player, extra) -> {
            if (tile instanceof IHasSortableFilters) {
                ((IHasSortableFilters) tile).moveUp(extra);
            }
        }),
        MOVE_FILTER_DOWN((tile, player, extra) -> {
            if (tile instanceof IHasSortableFilters) {
                ((IHasSortableFilters) tile).moveDown(extra);
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

        NEXT_SECURITY_MODE((tile, player, extra) -> {
            if (tile.hasSecurity()) {
                UUID owner = tile.getOwnerUUID();
                if (owner != null && player.getUUID().equals(owner)) {
                    tile.setSecurityMode(tile.getSecurityMode().getNext());
                }
            }
        }),

        SECURITY_DESK_MODE((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk) {
                ((TileEntitySecurityDesk) tile).setSecurityDeskMode(SecurityMode.byIndexStatic(extra));
            }
        }),

        NEXT_MODE((tile, player, extra) -> {
            if (tile instanceof IHasMode) {
                ((IHasMode) tile).nextMode();
            }
        }),
        NEXT_REDSTONE_CONTROL((tile, player, extra) -> tile.setControlType(tile.getControlType().getNext(mode -> mode != RedstoneControl.PULSE || tile.canPulse()))),
        ENCODE_FORMULA((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator) {
                ((TileEntityFormulaicAssemblicator) tile).encodeFormula();
            }
        }),
        STOCK_CONTROL_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator) {
                ((TileEntityFormulaicAssemblicator) tile).toggleStockControl();
            }
        }),
        CRAFT_SINGLE((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator) {
                ((TileEntityFormulaicAssemblicator) tile).craftSingle();
            }
        }),
        CRAFT_ALL((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator) {
                ((TileEntityFormulaicAssemblicator) tile).craftAll();
            }
        }),
        MOVE_ITEMS((tile, player, extra) -> {
            if (tile instanceof TileEntityFormulaicAssemblicator) {
                ((TileEntityFormulaicAssemblicator) tile).moveItems();
            }
        }),

        ROUND_ROBIN_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                ((TileEntityLogisticalSorter) tile).toggleRoundRobin();
            }
        }),
        SINGLE_ITEM_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                ((TileEntityLogisticalSorter) tile).toggleSingleItem();
            }
        }),
        CHANGE_COLOR((tile, player, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                ((TileEntityLogisticalSorter) tile).changeColor(TransporterUtils.readColor(extra));
            }
        }),

        OVERRIDE_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk) {
                ((TileEntitySecurityDesk) tile).toggleOverride();
            }
        }),
        REMOVE_TRUSTED((tile, player, extra) -> {
            if (tile instanceof TileEntitySecurityDesk) {
                ((TileEntitySecurityDesk) tile).removeTrusted(extra);
            }
        }),

        SET_TIME((tile, player, extra) -> {
            if (tile instanceof TileEntityLaserAmplifier) {
                ((TileEntityLaserAmplifier) tile).setDelay(extra);
            }
        }),

        ;

        private final TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile;

        GuiInteraction(TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, PlayerEntity player, int extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }

    public enum GuiInteractionEntity {
        NEXT_SECURITY_MODE((entity, player, extra) -> {
            if (entity instanceof ISecurityObject) {
                ISecurityObject security = (ISecurityObject) entity;
                if (security.hasSecurity()) {
                    UUID owner = security.getOwnerUUID();
                    if (owner != null && player.getUUID().equals(owner)) {
                        security.setSecurityMode(security.getSecurityMode().getNext());
                    }
                }
            }
        }),
        CONTAINER_STOP_TRACKING((entity, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer) {
                ((MekanismContainer) player.containerMenu).stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_SKIN_SELECT((entity, player, extra) -> {
            if (player.containerMenu instanceof MainRobitContainer) {
                MainRobitContainer container = (MainRobitContainer) player.containerMenu;
                container.startTracking(extra, container);
            }
        }),
        ;

        private final TriConsumer<Entity, PlayerEntity, Integer> consumerForEntity;

        GuiInteractionEntity(TriConsumer<Entity, PlayerEntity, Integer> consumerForEntity) {
            this.consumerForEntity = consumerForEntity;
        }

        public void consume(Entity entity, PlayerEntity player, int extra) {
            consumerForEntity.accept(entity, player, extra);
        }
    }

    private enum Type {
        ENTITY,
        ITEM,
        INT;
    }
}