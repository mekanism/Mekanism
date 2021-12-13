package mekanism.common.block.attribute;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.container.INamedContainerProvider;

public class AttributeGui implements Attribute {

    private final Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar;
    @Nullable
    private final ILangEntry customName;

    public AttributeGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar, @Nullable ILangEntry customName) {
        this.containerRegistrar = containerRegistrar;
        this.customName = customName;
    }

    public <TILE extends TileEntityMekanism> INamedContainerProvider getProvider(TILE tile) {
        return containerRegistrar.get().getProvider(customName == null ? TextComponentUtil.build(tile.getBlockType()) : customName.translate(), tile);
    }
}
