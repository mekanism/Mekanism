package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGuiInteract {

    private GuiInteraction interaction;
    private BlockPos tilePosition;
    private int extra;

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
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public static void handle(PacketGuiInteract message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
            if (tile != null) {
                message.interaction.consume(tile, player, message.extra);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiInteract pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.interaction);
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeVarInt(pkt.extra);
    }

    public static PacketGuiInteract decode(PacketBuffer buf) {
        return new PacketGuiInteract(buf.readEnumValue(GuiInteraction.class), buf.readBlockPos(), buf.readVarInt());
    }

    public enum GuiInteraction {//TODO: Cleanup this enum/the elements in it as it is rather disorganized order wise currently
        AUTO_SORT_BUTTON((tile, player, extra) -> {
            if (tile instanceof TileEntityFactory<?>) {
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
                TileComponentSecurity securityComponent = tile.getSecurity();
                UUID owner = securityComponent.getOwnerUUID();
                if (owner != null && player.getUniqueID().equals(owner)) {
                    securityComponent.setMode(securityComponent.getMode().getNext());
                }
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

        private TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile;

        GuiInteraction(TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, PlayerEntity player, int extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}