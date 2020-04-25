package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.multiblock.UpdateProtocol.FormationResult;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class TileEntityStructuralGlass extends CapabilityTileEntity implements IStructuralMultiblock, IConfigurable {

    public Coord4D master;

    private Map<BlockPos, BlockState> cachedNeighbors = new HashMap<>();

    public TileEntityStructuralGlass() {
        super(MekanismTileEntityTypes.STRUCTURAL_GLASS.getTileEntityType());
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    public Map<BlockPos, BlockState> getNeighborCache() {
        return cachedNeighbors;
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
    public void doUpdate(BlockPos neighborPos) {
        if (!shouldUpdate(neighborPos)) {
            return;
        }
        if (master != null) {
            TileEntity masterTile = MekanismUtils.getTileEntity(getWorld(), master.getPos());
            if (masterTile instanceof IMultiblock) {
                ((IMultiblock<?>) masterTile).doUpdate(neighborPos);
            } else {
                master = null;
            }
        } else {
            IMultiblock<?> multiblock = new ControllerFinder().find();
            if (multiblock != null) {
                multiblock.doUpdate(neighborPos);
            }
        }
    }

    @Override
    public MultiblockData<?> getMultiblockData() {
        if (master != null) {
            TileEntity masterTile = MekanismUtils.getTileEntity(getWorld(), master.getPos());
            if (masterTile instanceof IMultiblock) {
                return ((IMultiblock<?>) masterTile).getMultiblockData();
            }
        }
        return null;
    }

    @Override
    public void onPlace() {
        if (!world.isRemote()) {
            doUpdate(null);
        }
    }

    @Nullable
    @Override
    public Coord4D getController() {
        return master;
    }

    @Override
    public boolean canInterface(TileEntity controller) {
        return true;
    }

    @Override
    public void setController(Coord4D coord) {
        master = coord;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && master == null) {
            IMultiblock<?> multiblock = new ControllerFinder().find();
            if (multiblock instanceof TileEntityMultiblock && multiblock.getMultiblockData() == null) {
                FormationResult result = ((TileEntityMultiblock<?>) multiblock).getProtocol().doUpdate();
                if (!result.isFormed() && result.getResultText() != null) {
                    player.sendMessage(result.getResultText());
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
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