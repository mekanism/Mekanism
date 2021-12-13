package mekanism.common.inventory.container.type;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.IContainerFactory;

public abstract class BaseMekanismContainerType<T, CONTAINER extends Container, FACTORY> extends ContainerType<CONTAINER> {

    protected final FACTORY mekanismConstructor;
    protected final Class<T> type;

    protected BaseMekanismContainerType(Class<T> type, FACTORY mekanismConstructor, IContainerFactory<CONTAINER> constructor) {
        super(constructor);
        this.type = type;
        this.mekanismConstructor = mekanismConstructor;
    }
}