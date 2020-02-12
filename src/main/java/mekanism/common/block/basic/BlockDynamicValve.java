package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve>, ISupportsComparator,
      IHasGui<TileEntityDynamicValve>, IHasDescription {

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDynamicTank>> getContainerType() {
        return MekanismContainerTypes.DYNAMIC_TANK;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicValve tile) {
        return new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicValve> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_VALVE.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_DYNAMIC_VALVE;
    }
}