package mekanism.common.inventory.container.entity.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftingRobitContainer extends CraftingMenu implements IEntityContainer<EntityRobit>, ISecurityContainer {

    private final EntityRobit entity;

    public CraftingRobitContainer(int id, Inventory inv, EntityRobit robit) {
        super(id, inv, robit.getWorldPosCallable());
        this.entity = robit;
        entity.open(inv.player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return entity.isAlive();
    }

    @NotNull
    @Override
    public EntityRobit getEntity() {
        return entity;
    }

    @NotNull
    @Override
    public MenuType<?> getType() {
        return MekanismContainerTypes.CRAFTING_ROBIT.get();
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        entity.close(player);
    }

    @Nullable
    @Override
    public ICapabilityProvider getSecurityObject() {
        return entity;
    }
}