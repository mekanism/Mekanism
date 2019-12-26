package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.InductionMatrixContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockInductionCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityInductionCasing>, IHasGui<TileEntityInductionCasing>, IHasDescription {

    @Override
    public INamedContainerProvider getProvider(TileEntityInductionCasing tile) {
        return new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new InductionMatrixContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityInductionCasing> getTileType() {
        return MekanismTileEntityTypes.INDUCTION_CASING.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_INDUCTION_CASING;
    }
}