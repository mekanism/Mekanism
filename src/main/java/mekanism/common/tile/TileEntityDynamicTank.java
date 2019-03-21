package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.api.TileNetworkList;
import mekanism.common.block.BlockBasic;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<SynchronizedTankData> implements
      IFluidContainerManager {

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering
     * fluids.
     */
    public Set<ValveData> valveViewing = new HashSet<>();

    /**
     * The capacity this tank has on the client-side.
     */
    public int clientCapacity;

    public float prevScale;

    public TileEntityDynamicTank() {
        super("DynamicTank");
    }

    public TileEntityDynamicTank(String name) {
        super(name);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            if (structure != null && clientHasStructure && isRendering) {
                float targetScale =
                      (float) (structure.fluidStored != null ? structure.fluidStored.amount : 0) / clientCapacity;

                if (Math.abs(prevScale - targetScale) > 0.01) {
                    prevScale = (9 * prevScale + targetScale) / 10;
                }
            }

            if (!clientHasStructure || !isRendering) {
                for (ValveData data : valveViewing) {
                    TileEntityDynamicTank tileEntity = (TileEntityDynamicTank) data.location.getTileEntity(world);

                    if (tileEntity != null) {
                        tileEntity.clientHasStructure = false;
                    }
                }

                valveViewing.clear();
            }
        }

        if (!world.isRemote) {
            if (structure != null) {
                if (structure.fluidStored != null && structure.fluidStored.amount <= 0) {
                    structure.fluidStored = null;
                    markDirty();
                }

                if (isRendering) {
                    boolean needsValveUpdate = false;

                    for (ValveData data : structure.valves) {
                        if (data.activeTicks > 0) {
                            data.activeTicks--;
                        }

                        if (data.activeTicks > 0 != data.prevActive) {
                            needsValveUpdate = true;
                        }

                        data.prevActive = data.activeTicks > 0;
                    }

                    if (needsValveUpdate || structure.needsRenderUpdate()) {
                        sendPacketToRenderer();
                    }

                    structure.prevFluid = structure.fluidStored != null ? structure.fluidStored.copy() : null;

                    manageInventory();
                }
            }
        }
    }

    public void manageInventory() {
        int needed = (structure.volume * TankUpdateProtocol.FLUID_PER_TANK) - (structure.fluidStored != null
              ? structure.fluidStored.amount : 0);

        if (FluidContainerUtils.isFluidContainer(structure.inventory.get(0))) {
            structure.fluidStored = FluidContainerUtils
                  .handleContainerItem(this, structure.inventory, structure.editMode, structure.fluidStored, needed, 0,
                        1, null);

            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (!player.isSneaking() && structure != null) {
            if (!BlockBasic.manageInventory(player, this, hand, stack)) {
                Mekanism.packetHandler.sendToReceivers(
                      new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                      new Range4D(Coord4D.get(this)));
                player.openGui(Mekanism.instance, 18, world, getPos().getX(), getPos().getY(), getPos().getZ());
            } else {
                player.inventory.markDirty();
                sendPacketToRenderer();
            }

            return true;
        }

        return false;
    }

    @Override
    protected SynchronizedTankData getNewStructure() {
        return new SynchronizedTankData();
    }

    @Override
    public TankCache getNewCache() {
        return new TankCache();
    }

    @Override
    protected TankUpdateProtocol getProtocol() {
        return new TankUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedTankData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        if (structure != null) {
            data.add(structure.volume * TankUpdateProtocol.FLUID_PER_TANK);
            data.add(structure.editMode.ordinal());

            TileUtils.addFluidStack(data, structure.fluidStored);

            if (isRendering) {
                Set<ValveData> toSend = new HashSet<>();

                for (ValveData valveData : structure.valves) {
                    if (valveData.activeTicks > 0) {
                        toSend.add(valveData);
                    }
                }

                data.add(toSend.size());

                for (ValveData valveData : toSend) {
                    valveData.location.write(data);
                    data.add(valveData.side);
                }
            }
        }

        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (clientHasStructure) {
                clientCapacity = dataStream.readInt();
                structure.editMode = ContainerEditMode.values()[dataStream.readInt()];

                structure.fluidStored = TileUtils.readFluidStack(dataStream);

                if (isRendering) {
                    int size = dataStream.readInt();

                    valveViewing.clear();

                    for (int i = 0; i < size; i++) {
                        ValveData data = new ValveData();
                        data.location = Coord4D.read(dataStream);
                        data.side = EnumFacing.byIndex(dataStream.readInt());

                        valveViewing.add(data);

                        TileEntityDynamicTank tileEntity = (TileEntityDynamicTank) data.location.getTileEntity(world);

                        if (tileEntity != null) {
                            tileEntity.clientHasStructure = true;
                        }
                    }
                }
            }
        }
    }

    public int getScaledFluidLevel(long i) {
        if (clientCapacity == 0 || structure.fluidStored == null) {
            return 0;
        }

        return (int) (structure.fluidStored.amount * i / clientCapacity);
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        if (structure != null) {
            return structure.editMode;
        }

        return ContainerEditMode.BOTH;
    }

    @Override
    public void setContainerEditMode(ContainerEditMode mode) {
        if (structure == null) {
            return;
        }

        structure.editMode = mode;
    }
}
