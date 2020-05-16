package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;
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

    private MultiblockData multiblock = new MultiblockData(this);

    private Map<BlockPos, BlockState> cachedNeighbors = new HashMap<>();

    private long lastProtocolUpdate = -1;

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
        IMultiblock<?> master = getMaster();
        if (master != null) {
            return master.onActivate(player, hand, stack);
        }
        return ActionResultType.PASS;
    }

    @Override
    public void requestUpdate(BlockPos neighborPos, UpdateType type) {
        if (lastProtocolUpdate == getWorld().getGameTime() || !shouldUpdate(neighborPos)) {
            return;
        }
        if (multiblock.isFormed()) {
            IMultiblock<?> master = getMaster();
            if (master != null) {
                master.requestUpdate(neighborPos, UpdateType.FORCE);
            }
        } else {
            IMultiblock<?> multiblock = new ControllerFinder().find();
            if (multiblock != null) {
                multiblock.requestUpdate(neighborPos, UpdateType.FORCE);
            }
        }
    }

    @Override
    public void markUpdated() {
        lastProtocolUpdate = getWorld().getGameTime();
    }

    @Override
    public boolean updatedThisTick() {
        return lastProtocolUpdate == getWorld().getGameTime();
    }

    private IMultiblock<?> getMaster() {
        if (multiblock.isFormed() && multiblock.minLocation != null) {
            TileEntity masterTile = MekanismUtils.getTileEntity(getWorld(), multiblock.minLocation);
            if (masterTile instanceof IMultiblock) {
                return (IMultiblock<?>) masterTile;
            }
        }
        return null;
    }

    @Override
    public MultiblockData getMultiblock() {
        return multiblock;
    }

    @Override
    public void setMultiblock(MultiblockData multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public void removeMultiblock() {
        multiblock.remove(getWorld());
        multiblock = new MultiblockData(this);
    }

    @Override
    public boolean canInterface(TileEntity controller) {
        return true;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && !multiblock.isFormed()) {
            IMultiblock<?> multiblock = new ControllerFinder().find();
            if (multiblock instanceof TileEntityMultiblock && !multiblock.getMultiblock().isFormed()) {
                FormationResult result = ((TileEntityMultiblock<?>) multiblock).getProtocol().doUpdate(UpdateType.NORMAL);
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