package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.PacketHandler;
import mekanism.common.base.IGuiProvider;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.util.MekanismUtils;
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

    public static List<IGuiProvider> handlers = new ArrayList<>();

    private ClickedTileButton tileButton;
    private ClickedEntityButton entityButton;
    private boolean hasEntity;
    private int entityID;
    private BlockPos tilePosition;

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
    }

    public PacketGuiButtonPress(ClickedEntityButton buttonClicked, int entityID) {
        this.entityButton = buttonClicked;
        hasEntity = true;
        this.entityID = entityID;
    }

    public static void handle(PacketGuiButtonPress message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (!player.world.isRemote) {
                //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
                if (message.hasEntity) {
                    //TODO: send entity id back to server
                    Entity entity = player.world.getEntityByID(message.entityID);
                    if (entity != null) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, message.entityButton.getProvider(entity), buf -> buf.writeInt(message.entityID));
                    }
                } else {
                    //TODO: Send back what tile it was that got interacted with
                    TileEntity tile = MekanismUtils.getTileEntity(player.world, message.tilePosition);
                    if (tile != null) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, message.tileButton.getProvider(tile), message.tilePosition);
                    }
                }
            }
        }, player);
    }

    public static void encode(PacketGuiButtonPress pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.hasEntity);
        if (pkt.hasEntity) {
            buf.writeEnumValue(pkt.entityButton);
            buf.writeInt(pkt.entityID);
        } else {
            buf.writeEnumValue(pkt.tileButton);
            buf.writeBlockPos(pkt.tilePosition);
        }
    }

    public static PacketGuiButtonPress decode(PacketBuffer buf) {
        boolean hasEntity = buf.readBoolean();
        if (hasEntity) {
            return new PacketGuiButtonPress(buf.readEnumValue(ClickedEntityButton.class), buf.readInt());
        }
        return new PacketGuiButtonPress(buf.readEnumValue(ClickedTileButton.class), buf.readBlockPos());
    }


    //TODO: Add some built in way so that MekanismGenerators has an easy way to do this. TODO: Maybe have it be an interface? Except then we can't just write as enum
    public enum ClickedTileButton {
        GUI_BUTTON();

        private Function<TileEntity, INamedContainerProvider> providerFromTile;

        ClickedTileButton(Function<TileEntity, INamedContainerProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public INamedContainerProvider getProvider(TileEntity tile) {
            return providerFromTile.apply(tile);
        }
    }

    public enum ClickedEntityButton {
        //Entities
        ROBIT_CRAFTING(entity -> new ContainerProvider("mekanism.container.robit_crafting", (i, inv, player) -> {
            if (entity instanceof EntityRobit) {
                return new CraftingRobitContainer(i, inv, (EntityRobit) entity);
            }
            return null;
        })),
        ROBIT_INVENTORY(entity -> new ContainerProvider("mekanism.container.robit_inventory", (i, inv, player) -> {
            if (entity instanceof EntityRobit) {
                return new InventoryRobitContainer(i, inv, (EntityRobit) entity);
            }
            return null;
        })),
        ROBIT_MAIN(entity -> new ContainerProvider("mekanism.container.robit_main", (i, inv, player) -> {
            if (entity instanceof EntityRobit) {
                return new MainRobitContainer(i, inv, (EntityRobit) entity);
            }
            return null;
        })),
        ROBIT_REPAIR(entity -> new ContainerProvider("mekanism.container.robit_repair", (i, inv, player) -> {
            if (entity instanceof EntityRobit) {
                return new RepairRobitContainer(i, inv, (EntityRobit) entity);
            }
            return null;
        })),
        ROBIT_SMELTING(entity -> new ContainerProvider("mekanism.container.robit_smelting", (i, inv, player) -> {
            if (entity instanceof EntityRobit) {
                return new SmeltingRobitContainer(i, inv, (EntityRobit) entity);
            }
            return null;
        }));

        private Function<Entity, INamedContainerProvider> providerFromEntity;

        ClickedEntityButton(Function<Entity, INamedContainerProvider> providerFromEntity) {
            this.providerFromEntity = providerFromEntity;
        }

        public INamedContainerProvider getProvider(Entity entity) {
            return providerFromEntity.apply(entity);
        }
    }
}