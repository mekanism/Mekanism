package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockInductionCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityInductionCasing>, IHasGui<TileEntityInductionCasing>, IHasDescription {

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityInductionCasing>> getContainerType() {
        return MekanismContainerTypes.INDUCTION_MATRIX;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityInductionCasing tile) {
        return new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile));
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