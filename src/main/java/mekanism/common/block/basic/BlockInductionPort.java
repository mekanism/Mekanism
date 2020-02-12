package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionPort;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockInductionPort extends BlockBasicMultiblock implements IStateActive, IHasInventory, IHasTileEntity<TileEntityInductionPort>, ISupportsComparator,
      IHasGui<TileEntityInductionPort>, IHasDescription {

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityInductionCasing>> getContainerType() {
        return MekanismContainerTypes.INDUCTION_MATRIX;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityInductionPort tile) {
        return new ContainerProvider(MekanismLang.MATRIX, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.INDUCTION_MATRIX, i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityInductionPort> getTileType() {
        return MekanismTileEntityTypes.INDUCTION_PORT.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_INDUCTION_PORT;
    }
}