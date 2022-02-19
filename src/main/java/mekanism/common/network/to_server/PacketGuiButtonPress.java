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
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public class PacketGuiButtonPress implements IMekanismPacket {

    private final Type type;
    private ClickedItemButton itemButton;
    private ClickedTileButton tileButton;
    private ClickedEntityButton entityButton;
    private Hand hand;
    private int entityID;
    private int extra;
    private BlockPos tilePosition;

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, TileEntity tile) {
        this(buttonClicked, tile.getBlockPos());
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, TileEntity tile, int extra) {
        this(buttonClicked, tile.getBlockPos(), extra);
    }

    public PacketGuiButtonPress(ClickedTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGuiButtonPress(ClickedItemButton buttonClicked, Hand hand) {
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
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        if (type == Type.ENTITY) {
            Entity entity = player.level.getEntity(entityID);
            if (entity != null) {
                INamedContainerProvider provider = entityButton.getProvider(entity);
                if (provider != null) {
                    //Ensure valid data
                    NetworkHooks.openGui(player, provider, buf -> buf.writeVarInt(entityID));
                }
            }
        } else if (type == Type.TILE) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                INamedContainerProvider provider = tileButton.getProvider(tile, extra);
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
                INamedContainerProvider provider = itemButton.getProvider(stack, hand);
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
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        if (type == Type.ENTITY) {
            buffer.writeEnum(entityButton);
            buffer.writeVarInt(entityID);
        } else if (type == Type.TILE) {
            buffer.writeEnum(tileButton);
            buffer.writeBlockPos(tilePosition);
            buffer.writeVarInt(extra);
        } else if (type == Type.ITEM) {
            buffer.writeEnum(itemButton);
            buffer.writeEnum(hand);
        }
    }

    public static PacketGuiButtonPress decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        switch (type) {
            case ENTITY:
                return new PacketGuiButtonPress(buffer.readEnum(ClickedEntityButton.class), buffer.readVarInt());
            case TILE:
                return new PacketGuiButtonPress(buffer.readEnum(ClickedTileButton.class), buffer.readBlockPos(), buffer.readVarInt());
            case ITEM:
                return new PacketGuiButtonPress(buffer.readEnum(ClickedItemButton.class), buffer.readEnum(Hand.class));
            default:
                return null;
        }
    }

    public enum ClickedItemButton {
        BACK_BUTTON((stack, hand) -> {
            if (stack.getItem() instanceof IGuiItem) {
                return ((IGuiItem) stack.getItem()).getContainerType().getProvider(stack.getHoverName(), hand, stack);
            }
            return null;
        }),
        QIO_FREQUENCY_SELECT((stack, hand) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, hand, stack));

        private final BiFunction<ItemStack, Hand, INamedContainerProvider> providerFromItem;

        ClickedItemButton(BiFunction<ItemStack, Hand, INamedContainerProvider> providerFromItem) {
            this.providerFromItem = providerFromItem;
        }

        public INamedContainerProvider getProvider(ItemStack stack, Hand hand) {
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

        private final BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile;

        ClickedTileButton(BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public INamedContainerProvider getProvider(TileEntityMekanism tile, int extra) {
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

        private final Function<Entity, INamedContainerProvider> providerFromEntity;

        ClickedEntityButton(Function<Entity, INamedContainerProvider> providerFromEntity) {
            this.providerFromEntity = providerFromEntity;
        }

        public INamedContainerProvider getProvider(Entity entity) {
            return providerFromEntity.apply(entity);
        }
    }

    public enum Type {
        TILE,
        ITEM,
        ENTITY;
    }
}