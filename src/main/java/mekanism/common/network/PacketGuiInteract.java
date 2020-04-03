package mekanism.common.network;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import mekanism.common.PacketHandler;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGuiInteract {

    private GuiInteraction tileButton;
    private int extra;
    private BlockPos tilePosition;

    public PacketGuiInteract(GuiInteraction buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGuiInteract(GuiInteraction buttonClicked, BlockPos tilePosition, int extra) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public static void handle(PacketGuiInteract message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
            if (tile != null) {
                message.tileButton.consume(tile, message.extra);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGuiInteract pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.tileButton);
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeInt(pkt.extra);
    }

    public static PacketGuiInteract decode(PacketBuffer buf) {
        return new PacketGuiInteract(buf.readEnumValue(GuiInteraction.class), buf.readBlockPos(), buf.readInt());
    }

    public enum GuiInteraction {
        AUTO_SORT_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityFactory<?>) {
                ((TileEntityFactory<?>) tile).toggleSorting();
            }
        }),
        DUMP_BUTTON((tile, extra) -> {
            if (tile instanceof IHasDumpButton) {
                ((IHasDumpButton) tile).dump();
            }
        }),
        GAS_MODE_BUTTON((tile, extra) -> {
            if (tile instanceof IHasGasMode) {
                ((IHasGasMode) tile).nextMode(extra);
            }
        }),

        AUTO_EJECT_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleAutoEject();
            }
        }),
        AUTO_PULL_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleAutoPull();
            }
        }),
        INVERSE_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleInverse();
            }
        }),
        RESET_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).reset();
            }
        }),
        SILK_TOUCH_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).toggleSilkTouch();
            }
        }),
        START_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).start();
            }
        }),
        STOP_BUTTON((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).stop();
            }
        }),
        SET_RADIUS((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setRadiusFromPacket(extra);
            }
        }),
        SET_MIN_Y((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setMinYFromPacket(extra);
            }
        }),
        SET_MAX_Y((tile, extra) -> {
            if (tile instanceof TileEntityDigitalMiner) {
                ((TileEntityDigitalMiner) tile).setMaxYFromPacket(extra);
            }
        }),

        MOVE_FILTER_UP((tile, extra) -> {
            if (tile instanceof IHasSortableFilters) {
                ((IHasSortableFilters) tile).moveUp(extra);
            }
        }),
        MOVE_FILTER_DOWN((tile, extra) -> {
            if (tile instanceof IHasSortableFilters) {
                ((IHasSortableFilters) tile).moveDown(extra);
            }
        }),

        ;

        private BiConsumer<TileEntityMekanism, Integer> consumerForTile;

        GuiInteraction(BiConsumer<TileEntityMekanism, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, int extra) {
            consumerForTile.accept(tile, extra);
        }
    }
}