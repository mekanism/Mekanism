package mekanism.common.network;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.PacketHandler;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.inventory.container.tile.SideConfigurationContainer;
import mekanism.common.inventory.container.tile.TransporterConfigurationContainer;
import mekanism.common.inventory.container.tile.UpgradeManagementContainer;
import mekanism.common.inventory.container.tile.filter.DMItemStackFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMMaterialFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMModIDFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMTagFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSItemStackFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSModIDFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSTagFilterContainer;
import mekanism.common.inventory.container.tile.filter.OredictionificatorFilterContainer;
import mekanism.common.inventory.container.tile.filter.list.DigitalMinerConfigContainer;
import mekanism.common.inventory.container.tile.filter.select.DMFilterSelectContainer;
import mekanism.common.inventory.container.tile.filter.select.LSFilterSelectContainer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public class PacketGuiButtonPress {

    //TODO: Make a tab button pressed? and have a registry that encodes a resource location as a "key" so that the server and client can know
    // what one is being talked about without worrying about potential index mismatches
    // Decide if it makes sense to do this in a separate packet or as part of this
    private ClickedTileButton tileButton;
    private ClickedEntityButton entityButton;
    private boolean hasEntity;
    private int entityID;
    private int extra;
    private BlockPos tilePosition;

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition, int extra) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public PacketGuiButtonPress(ClickedEntityButton buttonClicked, int entityID) {
        this.entityButton = buttonClicked;
        hasEntity = true;
        this.entityID = entityID;
    }

    public static void handle(PacketGuiButtonPress message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (!player.world.isRemote) {
                //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
                if (message.hasEntity) {
                    Entity entity = player.world.getEntityByID(message.entityID);
                    if (entity != null) {
                        INamedContainerProvider provider = message.entityButton.getProvider(entity);
                        if (provider != null) {
                            //Ensure valid data
                            NetworkHooks.openGui((ServerPlayerEntity) player, provider, buf -> buf.writeInt(message.entityID));
                        }
                    }
                } else {
                    TileEntity tile = MekanismUtils.getTileEntity(player.world, message.tilePosition);
                    if (tile instanceof TileEntityMekanism) {
                        INamedContainerProvider provider = message.tileButton.getProvider((TileEntityMekanism) tile, message.extra);
                        if (provider != null) {
                            //Ensure valid data
                            NetworkHooks.openGui((ServerPlayerEntity) player, provider, message.tilePosition);
                        }
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiButtonPress pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.hasEntity);
        if (pkt.hasEntity) {
            buf.writeEnumValue(pkt.entityButton);
            buf.writeInt(pkt.entityID);
        } else {
            buf.writeEnumValue(pkt.tileButton);
            buf.writeBlockPos(pkt.tilePosition);
            buf.writeInt(pkt.extra);
        }
    }

    public static PacketGuiButtonPress decode(PacketBuffer buf) {
        boolean hasEntity = buf.readBoolean();
        if (hasEntity) {
            return new PacketGuiButtonPress(buf.readEnumValue(ClickedEntityButton.class), buf.readInt());
        }
        return new PacketGuiButtonPress(buf.readEnumValue(ClickedTileButton.class), buf.readBlockPos(), buf.readInt());
    }


    //TODO: Add some built in way so that MekanismGenerators has an easy way to do this.
    // Maybe have it be an interface? Except then we can't just write as enum
    public enum ClickedTileButton {
        BACK_BUTTON((tile, extra) -> {
            //Special handling to basically reset to the tiles default gui container
            Block block = tile.getBlockType();
            if (block instanceof IHasGui) {
                return ((IHasGui<TileEntityMekanism>) block).getProvider(tile);
            }
            return null;
        }),
        SIDE_CONFIGURATION((tile, extra) -> {
            return new ContainerProvider("mekanism.container.side_configuration", (i, inv, player) -> new SideConfigurationContainer(i, inv, tile));
        }),
        TRANSPORTER_CONFIGURATION((tile, extra) -> {
            return new ContainerProvider("mekanism.container.transporter_configuration", (i, inv, player) -> new TransporterConfigurationContainer(i, inv, tile));
        }),
        UPGRADE_MANAGEMENT((tile, extra) -> {
            return new ContainerProvider("mekanism.container.upgrade_management", (i, inv, player) -> new UpgradeManagementContainer(i, inv, tile));
        }),
        OREDICTIONIFICATOR_FILTER((tile, extra) -> {
            if (tile instanceof TileEntityOredictionificator) {
                return new ContainerProvider("mekanism.container.oredictionificator_filter", (i, inv, player) ->
                      new OredictionificatorFilterContainer(i, inv, (TileEntityOredictionificator) tile, extra));
            }
            return null;
        }),
        DIGITAL_MINER_CONFIG((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_config", (i, inv, player) ->
                      new DigitalMinerConfigContainer(i, inv, (TileEntityDigitalMiner) tile));
            }
            return null;
        }),
        DM_SELECT_FILTER_TYPE((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_filter_select", (i, inv, player) ->
                      new DMFilterSelectContainer(i, inv, (TileEntityDigitalMiner) tile));
            }
            return null;
        }),
        DM_FILTER_ITEMSTACK((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_itemstack_filter", (i, inv, player) ->
                      new DMItemStackFilterContainer(i, inv, (TileEntityDigitalMiner) tile, extra));
            }
            return null;
        }),
        DM_FILTER_MATERIAL((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_material_filter", (i, inv, player) ->
                      new DMMaterialFilterContainer(i, inv, (TileEntityDigitalMiner) tile, extra));
            }
            return null;
        }),
        DM_FILTER_MOD_ID((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_mod_id_filter", (i, inv, player) ->
                      new DMModIDFilterContainer(i, inv, (TileEntityDigitalMiner) tile, extra));
            }
            return null;
        }),
        DM_FILTER_TAG((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                return new ContainerProvider("mekanism.container.digital_miner_tag_filter", (i, inv, player) ->
                      new DMTagFilterContainer(i, inv, (TileEntityDigitalMiner) tile, extra));
            }
            return null;
        }),
        LS_SELECT_FILTER_TYPE((tile, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                return new ContainerProvider("mekanism.container.logistical_sorter_filter_select", (i, inv, player) ->
                      new LSFilterSelectContainer(i, inv, (TileEntityLogisticalSorter) tile));
            }
            return null;
        }),
        LS_FILTER_ITEMSTACK((tile, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                return new ContainerProvider("mekanism.container.logistical_sorter_itemstack_filter", (i, inv, player) ->
                      new LSItemStackFilterContainer(i, inv, (TileEntityLogisticalSorter) tile, extra));
            }
            return null;
        }),
        LS_FILTER_MATERIAL((tile, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                return new ContainerProvider("mekanism.container.logistical_sorter_material_filter", (i, inv, player) ->
                      new LSItemStackFilterContainer(i, inv, (TileEntityLogisticalSorter) tile, extra));
            }
            return null;
        }),
        LS_FILTER_MOD_ID((tile, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                return new ContainerProvider("mekanism.container.logistical_sorter_mod_id_filter", (i, inv, player) ->
                      new LSModIDFilterContainer(i, inv, (TileEntityLogisticalSorter) tile, extra));
            }
            return null;
        }),
        LS_FILTER_TAG((tile, extra) -> {
            if (tile instanceof TileEntityLogisticalSorter) {
                return new ContainerProvider("mekanism.container.logistical_sorter_tag_filter", (i, inv, player) ->
                      new LSTagFilterContainer(i, inv, (TileEntityLogisticalSorter) tile, extra));
            }
            return null;
        });

        private BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile;

        ClickedTileButton(BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public INamedContainerProvider getProvider(TileEntityMekanism tile, int extra) {
            return providerFromTile.apply(tile, extra);
        }
    }

    public enum ClickedEntityButton {
        //Entities
        ROBIT_CRAFTING(entity -> {
            if (entity instanceof EntityRobit) {
                return new ContainerProvider("mekanism.container.robit_crafting", (i, inv, player) -> new CraftingRobitContainer(i, inv, (EntityRobit) entity));
            }
            return null;
        }),
        ROBIT_INVENTORY(entity -> {
            if (entity instanceof EntityRobit) {
                return new ContainerProvider("mekanism.container.robit_inventory", (i, inv, player) -> new InventoryRobitContainer(i, inv, (EntityRobit) entity));
            }
            return null;
        }),
        ROBIT_MAIN(entity -> {
            if (entity instanceof EntityRobit) {
                return new ContainerProvider("mekanism.container.robit_main", (i, inv, player) -> new MainRobitContainer(i, inv, (EntityRobit) entity));
            }
            return null;
        }),
        ROBIT_REPAIR(entity -> {
            if (entity instanceof EntityRobit) {
                return new ContainerProvider("mekanism.container.robit_repair", (i, inv, player) -> new RepairRobitContainer(i, inv, (EntityRobit) entity));
            }
            return null;
        }),
        ROBIT_SMELTING(entity -> {
            if (entity instanceof EntityRobit) {
                return new ContainerProvider("mekanism.container.robit_smelting", (i, inv, player) -> new SmeltingRobitContainer(i, inv, (EntityRobit) entity));
            }
            return null;
        });

        private Function<Entity, INamedContainerProvider> providerFromEntity;

        ClickedEntityButton(Function<Entity, INamedContainerProvider> providerFromEntity) {
            this.providerFromEntity = providerFromEntity;
        }

        public INamedContainerProvider getProvider(Entity entity) {
            return providerFromEntity.apply(entity);
        }
    }
}