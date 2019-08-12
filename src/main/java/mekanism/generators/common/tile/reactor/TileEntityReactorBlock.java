package mekanism.generators.common.tile.reactor;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.base.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.generators.common.FusionReactor;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class TileEntityReactorBlock extends TileEntityMekanism {

    public FusionReactor fusionReactor;

    public boolean attempted;

    public boolean changed;

    public TileEntityReactorBlock() {
        //TODO: Does hierarchy have to be done this way
        this(GeneratorsBlock.REACTOR_FRAME);
    }

    public TileEntityReactorBlock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    public abstract boolean isFrame();

    public FusionReactor getReactor() {
        return fusionReactor;
    }

    public void setReactor(FusionReactor reactor) {
        if (reactor != fusionReactor) {
            changed = true;
        }
        fusionReactor = reactor;
    }

    @Override
    public void remove() {
        super.remove();
        if (getReactor() != null) {
            getReactor().formMultiblock(false);
        }
    }

    @Override
    public void onUpdate() {
        if (changed) {
            changed = false;
        }
        if (!world.isRemote && ticker == 5 && !attempted && (getReactor() == null || !getReactor().isFormed())) {
            updateController();
        }
        attempted = false;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!(this instanceof TileEntityReactorController) && getReactor() != null) {
            getReactor().formMultiblock(true);
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (!world.isRemote) {
            if (getReactor() != null) {
                getReactor().formMultiblock(false);
            } else {
                updateController();
            }
        }
    }

    public void updateController() {
        if (!(this instanceof TileEntityReactorController)) {
            TileEntityReactorController found = new ControllerFinder().find();
            if (found != null && (found.getReactor() == null || !found.getReactor().isFormed())) {
                found.formMultiblock(false);
            }
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    public class ControllerFinder {

        public TileEntityReactorController found;

        public Set<Coord4D> iterated = new HashSet<>();

        public void loop(Coord4D pos) {
            if (iterated.size() > 512 || found != null) {
                return;
            }

            iterated.add(pos);
            for (Direction side : Direction.values()) {
                Coord4D coord = pos.offset(side);
                if (!iterated.contains(coord) && coord.getTileEntity(world) instanceof TileEntityReactorBlock) {
                    ((TileEntityReactorBlock) coord.getTileEntity(world)).attempted = true;
                    if (coord.getTileEntity(world) instanceof TileEntityReactorController) {
                        found = (TileEntityReactorController) coord.getTileEntity(world);
                        return;
                    }
                    loop(coord);
                }
            }
        }

        public TileEntityReactorController find() {
            loop(Coord4D.get(TileEntityReactorBlock.this));
            return found;
        }
    }
}