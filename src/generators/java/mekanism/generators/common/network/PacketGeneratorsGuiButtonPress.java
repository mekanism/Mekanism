package mekanism.generators.common.network;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.inventory.container.reactor.info.ReactorFuelContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorHeatContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorStatsContainer;
import mekanism.generators.common.inventory.container.turbine.TurbineContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
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
public class PacketGeneratorsGuiButtonPress {

    private ClickedGeneratorsTileButton tileButton;
    private int extra;
    private BlockPos tilePosition;

    public PacketGeneratorsGuiButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos tilePosition) {
        this(buttonClicked, tilePosition, 0);
    }

    public PacketGeneratorsGuiButtonPress(ClickedGeneratorsTileButton buttonClicked, BlockPos tilePosition, int extra) {
        this.tileButton = buttonClicked;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public static void handle(PacketGeneratorsGuiButtonPress message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (!player.world.isRemote) {
                //If we are on the server (the only time we should be receiving this packet), let forge handle switching the Gui
                TileEntity tile = MekanismUtils.getTileEntity(player.world, message.tilePosition);
                if (tile instanceof TileEntityMekanism) {
                    INamedContainerProvider provider = message.tileButton.getProvider((TileEntityMekanism) tile, message.extra);
                    if (provider != null) {
                        //Ensure valid data
                        NetworkHooks.openGui((ServerPlayerEntity) player, provider, buf -> {
                            buf.writeBlockPos(message.tilePosition);
                            buf.writeInt(message.extra);
                        });
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGeneratorsGuiButtonPress pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.tileButton);
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeInt(pkt.extra);
    }

    public static PacketGeneratorsGuiButtonPress decode(PacketBuffer buf) {
        return new PacketGeneratorsGuiButtonPress(buf.readEnumValue(ClickedGeneratorsTileButton.class), buf.readBlockPos(), buf.readInt());
    }

    public enum ClickedGeneratorsTileButton {
        TAB_MAIN((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return new ContainerProvider("mekanism.container.industrial_turbine", (i, inv, player) -> new TurbineContainer(i, inv,
                      (TileEntityTurbineCasing) tile));
            }
            return null;
        }),
        TAB_HEAT((tile, extra) -> {
            if (tile instanceof TileEntityReactorController) {
                return new ContainerProvider("mekanism.container.reactor_heat", (i, inv, player) -> new ReactorHeatContainer(i, inv,
                      (TileEntityReactorController) tile));
            }
            return null;
        }),
        TAB_FUEL((tile, extra) -> {
            if (tile instanceof TileEntityReactorController) {
                return new ContainerProvider("mekanism.container.reactor_fuel", (i, inv, player) -> new ReactorFuelContainer(i, inv,
                      (TileEntityReactorController) tile));
            }
            return null;
        }),
        TAB_STATS((tile, extra) -> {
            if (tile instanceof TileEntityTurbineCasing) {
                return new ContainerProvider("mekanism.container.industrial_turbine", (i, inv, player) -> new TurbineContainer(i, inv,
                      (TileEntityTurbineCasing) tile));
            } else if (tile instanceof TileEntityReactorController) {
                return new ContainerProvider("mekanism.container.reactor_stats", (i, inv, player) -> new ReactorStatsContainer(i, inv,
                      (TileEntityReactorController) tile));
            }
            return null;
        });

        private BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile;

        ClickedGeneratorsTileButton(BiFunction<TileEntityMekanism, Integer, INamedContainerProvider> providerFromTile) {
            this.providerFromTile = providerFromTile;
        }

        public INamedContainerProvider getProvider(TileEntityMekanism tile, int extra) {
            return providerFromTile.apply(tile, extra);
        }
    }
}