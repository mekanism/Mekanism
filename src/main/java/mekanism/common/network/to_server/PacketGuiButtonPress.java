package mekanism.common.network.to_server;

import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public class PacketGuiButtonPress implements IMekanismPacket {

    private final Type type;
    private ClickedItemButton itemButton;
    private ClickedTileButton tileButton;
    private ClickedEntityButton entityButton;
    private InteractionHand hand;
    private int entityID;
    private int extra;
    private BlockPos tilePosition;

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockEntity tile) {
        this(buttonClicked, tile.getBlockPos());
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockEntity tile, int extra) {
        this(buttonClicked, tile.getBlockPos(), extra);
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGuiButtonPress(ClickedItemButton buttonClicked, InteractionHand hand) {
        type = Type.ITEM;
        this.itemButton = buttonClicked;
        this.hand = hand;
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition, int extra) {
        type = Type.TILE;
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public PacketGuiButtonPress(ClickedEntityButton buttonClicked, Entity entity) {
        this(buttonClicked, entity.getId());
    }

    public PacketGuiButtonPress(ClickedEntityButton buttonClicked, int entityID) {
        type = Type.ENTITY;
        this.entityButton = buttonClicked;
        this.entityID = entityID;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (type == Type.ENTITY) {
            Entity entity = player.level.getEntity(entityID);
            if (entity != null) {
                MenuProvider provider = entityButton.getProvider(entity);
                if (provider != null) {
                    //Ensure valid data
                    NetworkHooks.openGui(player, provider, buf -> buf.writeVarInt(entityID));
                }
            }
        } else if (type == Type.TILE) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                MenuProvider provider = tileButton.getProvider(tile, extra);
                if (provider != null) {
                    //Ensure valid data
                    NetworkHooks.openGui(player, provider, buf -> {
                        buf.writeBlockPos(tilePosition);
                        buf.writeVarInt(extra);
                    });
                }
            }
        } else if (type == Type.ITEM) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof IGuiItem) {
                MenuProvider provider = itemButton.getProvider(stack, hand);
                if (provider != null) {
                    NetworkHooks.openGui(player, provider, buf -> {
                        buf.writeEnum(hand);
                        buf.writeItem(stack);
                    });
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        switch (type) {
            case ENTITY -> {
                buffer.writeEnum(entityButton);
                buffer.writeVarInt(entityID);
            }
            case TILE -> {
                buffer.writeEnum(tileButton);
                buffer.writeBlockPos(tilePosition);
                buffer.writeVarInt(extra);
            }
            case ITEM -> {
                buffer.writeEnum(itemButton);
                buffer.writeEnum(hand);
            }
        }
    }

    public static PacketGuiButtonPress decode(FriendlyByteBuf buffer) {
        return switch (buffer.readEnum(Type.class)) {
            case ENTITY -> new PacketGuiButtonPress(buffer.readEnum(ClickedEntityButton.class), buffer.readVarInt());
            case TILE -> new PacketGuiButtonPress(buffer.readEnum(ClickedTileButton.class), buffer.readBlockPos(), buffer.readVarInt());
            case ITEM -> new PacketGuiButtonPress(buffer.readEnum(ClickedItemButton.class), buffer.readEnum(InteractionHand.class));
        };
    }

    public enum ClickedItemButton {
        BACK_BUTTON((stack, hand) -> {
            if (stack.getItem() instanceof IGuiItem guiItem) {
                return guiItem.getContainerType().getProvider(stack.getHoverName(), hand, stack);
            }
            return null;
        }),
        QIO_FREQUENCY_SELECT((stack, hand) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, hand, stack));

        private final BiFunction<ItemStack, InteractionHand, MenuProvider> providerFromItem;

        ClickedItemButton(BiFunction<ItemStack, InteractionHand, MenuProvider> providerFromItem) {
            this.providerFromItem = providerFromItem;
        }

        public MenuProvider getProvider(ItemStack stack, InteractionHand hand) {
            return providerFromItem.apply(stack, hand);
        }
    }

    public enum ClickedTileButton {
        BACK_BUTTON((tile, extra) -> {
            //Special handling to basically reset to the tiles default gui container
            Block block = tile.getBlockType();
            if (Attribute.has(block, AttributeGui.class)) {
                return Attribute.get(block, AttributeGui.class).getProvider(tile);
            }
            return null;
        }),
        QIO_FREQUENCY_SELECT((tile, extra) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, tile)),
        DIGITAL_MINER_CONFIG((tile, extra) -> MekanismContainerTypes.DIGITAL_MINER_CONFIG.getProvider(MekanismLang.MINER_CONFIG, tile)),

        TAB_MAIN((tile, extra) -> {
            if (tile instanceof TileEntityInductionCasing) {
                return MekanismContainerTypes.INDUCTION_MATRIX.getProvider(MekanismLang.MATRIX, tile);
            } else if (tile instanceof TileEntityBoilerCasing) {
                return MekanismContainerTypes.THERMOELECTRIC_BOILER.getProvider(MekanismLang.BOILER, tile);
            }
            return null;
        }),
        TAB_STATS((tile, extra) -> {
            if (tile instanceof TileEntityInductionCasing) {
                return MekanismContainerTypes.MATRIX_STATS.getProvider(MekanismLang.MATRIX_STATS, tile);
            } else if (tile instanceof TileEntityBoilerCasing) {
                return MekanismContainerTypes.BOILER_STATS.getProvider(MekanismLang.BOILER_STATS, tile);
            }
            return null;
        });

        private final BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile;

        ClickedTileButton(BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public MenuProvider getProvider(TileEntityMekanism tile, int extra) {
            return providerFromTile.apply(tile, extra);
        }
    }

    public enum ClickedEntityButton {
        //Entities
        ROBIT_CRAFTING(entity -> MekanismContainerTypes.CRAFTING_ROBIT.getProvider(MekanismLang.ROBIT_CRAFTING, entity)),
        ROBIT_INVENTORY(entity -> MekanismContainerTypes.INVENTORY_ROBIT.getProvider(MekanismLang.ROBIT_INVENTORY, entity)),
        ROBIT_MAIN(entity -> MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, entity)),
        ROBIT_REPAIR(entity -> MekanismContainerTypes.REPAIR_ROBIT.getProvider(MekanismLang.ROBIT_REPAIR, entity)),
        ROBIT_SMELTING(entity -> MekanismContainerTypes.SMELTING_ROBIT.getProvider(MekanismLang.ROBIT_SMELTING, entity));

        private final Function<Entity, MenuProvider> providerFromEntity;

        ClickedEntityButton(Function<Entity, MenuProvider> providerFromEntity) {
            this.providerFromEntity = providerFromEntity;
        }

        public MenuProvider getProvider(Entity entity) {
            return providerFromEntity.apply(entity);
        }
    }

    public enum Type {
        TILE,
        ITEM,
        ENTITY;
    }
}