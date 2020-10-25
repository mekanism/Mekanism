package mekanism.common.network;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGuiInteract {

    private final Type interactionType;

    private GuiInteraction interaction;
    private GuiInteractionItem itemInteraction;
    private GuiInteractionEntity entityInteraction;
    private BlockPos tilePosition;
    private ItemStack extraItem;
    private int entityID;
    private int extra;

    public PacketGuiInteract(GuiInteractionEntity interaction, int entityID) {
        this.interactionType = Type.ENTITY;
        this.entityInteraction = interaction;
        this.entityID = entityID;
    }

    public PacketGuiInteract(GuiInteraction interaction, TileEntity tile) {
        this(interaction, tile.getPos());
    }

    public PacketGuiInteract(GuiInteraction interaction, TileEntity tile, int extra) {
        this(interaction, tile.getPos(), extra);
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
        this(interaction, tile.getPos(), stack);
    }

    public PacketGuiInteract(GuiInteractionItem interaction, BlockPos tilePosition, ItemStack stack) {
        this.interactionType = Type.ITEM;
        this.itemInteraction = interaction;
        this.tilePosition = tilePosition;
        this.extraItem = stack;
    }

    public static void handle(PacketGuiInteract message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            if (player != null) {
                if (message.interactionType == Type.ENTITY) {
                    Entity entity = player.world.getEntityByID(message.entityID);
                    if (entity != null) {
                        message.entityInteraction.consume(entity, player);
                    }
                } else {
                    TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
                    if (tile != null) {
                        if (message.interactionType == Type.INT) {
                            message.interaction.consume(tile, player, message.extra);
                        } else if (message.interactionType == Type.ITEM) {
                            message.itemInteraction.consume(tile, player, message.extraItem);
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketGuiInteract pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.interactionType);
        if (pkt.interactionType == Type.ENTITY) {
            buf.writeEnumValue(pkt.entityInteraction);
            buf.writeVarInt(pkt.entityID);
        } else if (pkt.interactionType == Type.INT) {
            buf.writeEnumValue(pkt.interaction);
            buf.writeBlockPos(pkt.tilePosition);
            buf.writeVarInt(pkt.extra);
        } else if (pkt.interactionType == Type.ITEM) {
            buf.writeEnumValue(pkt.itemInteraction);
            buf.writeBlockPos(pkt.tilePosition);
            buf.writeItemStack(pkt.extraItem);
        }
    }

    public static PacketGuiInteract decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        if (type == Type.ENTITY) {
            return new PacketGuiInteract(buf.readEnumValue(GuiInteractionEntity.class), buf.readVarInt());
        } else if (type == Type.INT) {
            return new PacketGuiInteract(buf.readEnumValue(GuiInteraction.class), buf.readBlockPos(), buf.readVarInt());
        } else if (type == Type.ITEM) {
            return new PacketGuiInteract(buf.readEnumValue(GuiInteractionItem.class), buf.readBlockPos(), buf.readItemStack());
        }
        Mekanism.logger.error("Received malformed GUI interaction packet.");
        return null;
    }

    public enum GuiInteractionItem {
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
            if (player.openContainer instanceof MekanismContainer) {
                ((MekanismContainer) player.openContainer).stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_EJECTOR((tile, player, extra) -> {
            if (player.openContainer instanceof MekanismContainer && tile instanceof ISideConfiguration) {
                ((MekanismContainer) player.openContainer).startTracking(extra, ((ISideConfiguration) tile).getEjector());
            }
        }),
        CONTAINER_TRACK_SIDE_CONFIG((tile, player, extra) -> {
            if (player.openContainer instanceof MekanismContainer && tile instanceof ISideConfiguration) {
                ((MekanismContainer) player.openContainer).startTracking(extra, ((ISideConfiguration) tile).getConfig());
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
                TileComponentUpgrade componentUpgrade = tile.getComponent();
                Upgrade upgradeType = Upgrade.byIndexStatic(extra);
                if (componentUpgrade.getUpgrades(upgradeType) > 0 && player.inventory.addItemStackToInventory(UpgradeUtils.getStack(upgradeType))) {
                    componentUpgrade.removeUpgrade(upgradeType);
                }
            }
        }),

        NEXT_SECURITY_MODE((tile, player, extra) -> {
            if (tile.hasSecurity()) {
                UUID owner = tile.getOwnerUUID();
                if (owner != null && player.getUniqueID().equals(owner)) {
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
                ((TileEntityLaserAmplifier) tile).setTime(extra);
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
        NEXT_SECURITY_MODE((entity, player) -> {
            if (entity instanceof ISecurityObject) {
                ISecurityObject security = (ISecurityObject) entity;
                if (security.hasSecurity()) {
                    UUID owner = security.getOwnerUUID();
                    if (owner != null && player.getUniqueID().equals(owner)) {
                        security.setSecurityMode(security.getSecurityMode().getNext());
                    }
                }
            }
        }),
        ;

        private final BiConsumer<Entity, PlayerEntity> consumerForEntity;

        GuiInteractionEntity(BiConsumer<Entity, PlayerEntity> consumerForTile) {
            this.consumerForEntity = consumerForTile;
        }

        public void consume(Entity entity, PlayerEntity player) {
            consumerForEntity.accept(entity, player);
        }
    }

    private enum Type {
        ENTITY,
        ITEM,
        INT;
    }
}