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
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockBoilerCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityBoilerCasing>, IHasGui<TileEntityBoilerCasing>, IHasDescription {

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBoilerCasing>> getContainerType() {
        return MekanismContainerTypes.THERMOELECTRIC_BOILER;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityBoilerCasing tile) {
        return new ContainerProvider(MekanismLang.BOILER, (i, inv, player) -> new MekanismTileContainer<>(MekanismContainerTypes.THERMOELECTRIC_BOILER, i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityBoilerCasing> getTileType() {
        return MekanismTileEntityTypes.BOILER_CASING.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_BOILER_CASING;
    }
}