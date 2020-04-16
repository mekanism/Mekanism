package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TileEntityThermalEvaporationBlock extends TileEntityMekanism {

    public Coord4D master;
    private boolean attempted;

    public TileEntityThermalEvaporationBlock() {
        this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK);
    }

    public TileEntityThermalEvaporationBlock(IBlockProvider provider) {
        super(provider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ticker == 5 && !attempted && master == null) {
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
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
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
        if (!isRemote()) {
            TileEntityThermalEvaporationController tile = getController();
            if (tile == null) {
                updateController();
            } else {
                tile.refresh();
            }
        }
    }

    private void updateController() {
        if (!(this instanceof TileEntityThermalEvaporationController)) {
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos checkPos = pos.offset(side);
                TileEntityThermalEvaporationController check = MekanismUtils.getTileEntity(TileEntityThermalEvaporationController.class, getWorld(), checkPos);
                if (check != null) {
                    check.refresh();
                    return;
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
            return MekanismUtils.getTileEntity(TileEntityThermalEvaporationController.class, getWorld(), master.getPos());
        }
        return null;
    }

    public class ControllerFinder {

        public TileEntityThermalEvaporationController found;

        public Set<BlockPos> iterated = new ObjectOpenHashSet<>();

        private Deque<BlockPos> checkQueue = new LinkedList<>();

        public void loop(BlockPos startPos) {
            checkQueue.add(startPos);

            while (checkQueue.peek() != null) {
                BlockPos checkPos = checkQueue.pop();
                if (iterated.contains(checkPos)) {
                    continue;
                }
                iterated.add(checkPos);

                TileEntity te = MekanismUtils.getTileEntity(getWorld(), checkPos);
                if (te instanceof TileEntityThermalEvaporationController) {
                    found = (TileEntityThermalEvaporationController) te;
                    return;
                }

                if (te instanceof TileEntityThermalEvaporationBlock) {
                    ((TileEntityThermalEvaporationBlock) te).attempted = true;
                    for (Direction side : EnumUtils.DIRECTIONS) {
                        BlockPos coord = checkPos.offset(side);
                        if (!iterated.contains(coord)) {
                            checkQueue.addLast(coord);
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