package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
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
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicTank extends BlockBasicMultiblock implements IHasModel, IHasTileEntity<TileEntityDynamicTank>, IHasGui<TileEntityDynamicTank>, IHasDescription {

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDynamicTank>> getContainerType() {
        return MekanismContainerTypes.DYNAMIC_TANK;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicTank tile) {
        return new ContainerProvider(MekanismLang.DYNAMIC_TANK, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.DYNAMIC_TANK, i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicTank> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_TANK.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_DYNAMIC_TANK;
    }
}