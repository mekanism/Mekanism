package mekanism.common.tile;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraft.util.math.BlockPos;

public class TileEntityThermalEvaporationBlock extends TileEntityContainerBlock implements IComputerIntegration {

    private static final String[] methods = new String[]{"getTemperature", "getHeight", "isFormed", "getInput",
          "getOutput"};
    public Coord4D master;
    public boolean attempted;

    public TileEntityThermalEvaporationBlock() {
        super("ThermalEvaporationBlock");

        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    public TileEntityThermalEvaporationBlock(String fullName) {
        super(fullName);

        inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote && ticker == 5 && !attempted && master == null) {
            updateController();
        }

        attempted = false;
    }

    public void addToStructure(Coord4D controller) {
        master = controller;
    }

    public void controllerGone() {
        master = null;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if (master != null) {
            TileEntityThermalEvaporationController tile = getController();

            if (tile != null) {
                tile.refresh();
            }
        }
    }

    @Override
    public void onNeighborChange(Block block) {
        super.onNeighborChange(block);

        if (!world.isRemote) {
            TileEntityThermalEvaporationController tile = getController();

            if (tile != null) {
                tile.refresh();
            } else {
                updateController();
            }
        }
    }

    public void updateController() {
        if (!(this instanceof TileEntityThermalEvaporationController)) {
            for (EnumFacing side : EnumFacing.values()){
                BlockPos checkPos = pos.offset(side);
                TileEntity check;
                if (world.isBlockLoaded(checkPos) && (check = world.getTileEntity(checkPos)) instanceof TileEntityThermalEvaporationBlock) {
                    if (check instanceof TileEntityThermalEvaporationController){
                        ((TileEntityThermalEvaporationController) check).refresh();
                        return;
                    }
                }
            }
            TileEntityThermalEvaporationController found = new ControllerFinder().find();

            if (found != null) {
                found.refresh();
            }
        }
    }

    public TileEntityThermalEvaporationController getController() {
        if (master != null) {
            TileEntity tile = master.getTileEntity(world);

            if (tile instanceof TileEntityThermalEvaporationController) {
                return (TileEntityThermalEvaporationController) tile;
            }
        }

        return null;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] args) throws NoSuchMethodException {
        TileEntityThermalEvaporationController controller = getController();

        if (controller == null) {
            return new Object[]{"Unformed."};
        }

        switch (method) {
            case 0:
                return new Object[]{controller.temperature};
            case 1:
                return new Object[]{controller.height};
            case 2:
                return new Object[]{controller.structured};
            case 3:
                return new Object[]{controller.inputTank.getFluidAmount()};
            case 4:
                return new Object[]{controller.outputTank.getFluidAmount()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    public class ControllerFinder {

        public TileEntityThermalEvaporationController found;

        public Set<BlockPos> iterated = new HashSet<>();

        private Deque<BlockPos> checkQueue = new LinkedList<>();

        public void loop(BlockPos startPos) {
            checkQueue.add(startPos);

            while (checkQueue.peek() != null) {
                BlockPos checkPos = checkQueue.pop();
                if (iterated.contains(checkPos)) {
                    continue;
                }
                iterated.add(checkPos);
                if (world.isBlockLoaded(checkPos)) {
                    TileEntity te = world.getTileEntity(checkPos);
                    if (te instanceof TileEntityThermalEvaporationController) {
                        found = (TileEntityThermalEvaporationController) te;
                        return;
                    }
                    if (te instanceof TileEntityThermalEvaporationBlock) {
                        ((TileEntityThermalEvaporationBlock) te).attempted = true;
                        for (EnumFacing side : EnumFacing.VALUES) {
                            BlockPos coord = checkPos.offset(side);
                            if (!iterated.contains(coord)) {
                                checkQueue.addLast(coord);
                            }
                        }
                    }
                }
            }
        }

        public TileEntityThermalEvaporationController find() {
            loop(TileEntityThermalEvaporationBlock.this.pos);

            return found;
        }
    }
}
