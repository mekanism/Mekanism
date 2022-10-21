package mekanism.common.inventory.container.entity;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    @NotNull
    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, @NotNull ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @NotNull
    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return entity.isAlive();
    }

    @Nullable
    @Override
    public ICapabilityProvider getSecurityObject() {
        return entity;
    }
}