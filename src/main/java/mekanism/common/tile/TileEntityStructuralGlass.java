package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;

//TODO: Why does this not extend TileEntityMekanism
public class TileEntityStructuralGlass extends TileEntity implements IStructuralMultiblock {

    public Coord4D master;

    public TileEntityStructuralGlass() {
        super(MekanismTileEntityTypes.STRUCTURAL_GLASS.getTileEntityType());
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (master != null) {
            TileEntity masterTile = MekanismUtils.getTileEntity(getWorld(), master.getPos());
            if (masterTile instanceof IMultiblock) {
                return ((IMultiblock<?>) masterTile).onActivate(player, hand, stack);
            }
            master = null;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void doUpdate() {
        if (master != null) {
            TileEntity masterTile = MekanismUtils.getTileEntity(getWorld(), master.getPos());
            if (masterTile instanceof IMultiblock) {
                ((IMultiblock<?>) masterTile).doUpdate();
            } else {
                master = null;
            }
        } else {
            IMultiblock<?> multiblock = new ControllerFinder().find();
            if (multiblock != null) {
                multiblock.doUpdate();
            }
        }
    }

    @Override
    public boolean canInterface(TileEntity controller) {
        return true;
    }

    @Override
    public void setController(Coord4D coord) {
        master = coord;
    }

    public class ControllerFinder {

        public IMultiblock<?> found;

        public Set<Coord4D> iterated = new ObjectOpenHashSet<>();

        public void loop(Coord4D pos) {
            if (iterated.size() > 2048 || found != null) {
                return;
            }
            iterated.add(pos);
            for (Direction side : EnumUtils.DIRECTIONS) {
                Coord4D coord = pos.offset(side);
                TileEntity tile = MekanismUtils.getTileEntity(getWorld(), coord.getPos());
                if (!iterated.contains(coord)) {
                    if (tile instanceof IMultiblock) {
                        found = (IMultiblock<?>) tile;
                        return;
                    } else if (tile instanceof IStructuralMultiblock) {
                        loop(coord);
                    }
                }
            }
        }

        public IMultiblock<?> find() {
            loop(Coord4D.get(TileEntityStructuralGlass.this));
            return found;
        }
    }
}