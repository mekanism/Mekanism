package mekanism.common.tile;

import mekanism.api.IConfigurable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;

public class TileEntityStructuralGlass extends CapabilityTileEntity implements IStructuralMultiblock, IConfigurable, ITickableTileEntity {

    private Structure structure = Structure.INVALID;

    private MultiblockData defaultMultiblock = new MultiblockData(this);

    public TileEntityStructuralGlass() {
        super(MekanismTileEntityTypes.STRUCTURAL_GLASS.getTileEntityType());
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    public MultiblockData getDefaultData() {
        return defaultMultiblock;
    }

    @Override
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    @Override
    public Structure getStructure() {
        return structure;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            structure.tick(this);
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        IMultiblock<?> master = getStructure().getController();
        if (master != null) {
            return master.onActivate(player, hand, stack);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canInterface(MultiblockManager<?> manager) {
        return true;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && !getMultiblockData().isFormed()) {
            IMultiblock<?> master = getStructure().getController();
            if (master != null) {
                FormationResult result = getStructure().runUpdate(this);
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

    @Override
    public void remove() {
        super.remove();
        if (!world.isRemote()) {
            structure.invalidate(world);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!world.isRemote()) {
            structure.invalidate(world);
        }
    }
}