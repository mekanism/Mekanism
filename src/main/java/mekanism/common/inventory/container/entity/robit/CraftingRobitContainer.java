package mekanism.common.inventory.container.entity.robit;

import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;

public class CraftingRobitContainer extends CraftingMenu implements IEntityContainer<EntityRobit>, ISecurityContainer {

    private final EntityRobit entity;

    public CraftingRobitContainer(int id, Inventory inv, EntityRobit robit) {
        super(id, inv, robit.getWorldPosCallable());
        this.entity = robit;
        entity.open(inv.player);
    }

    @Override
    public boolean stillValid(Player player) {
        return entity.isAlive();
    }

    @Override
    public EntityRobit getEntity() {
        return entity;
    }

    @Override
    public MenuType<?> getType() {
        return MekanismContainerTypes.CRAFTING_ROBIT.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        entity.close(player);
    }

    @Override
    public boolean canPlayerAccess(Player player) {
        return IEntitySecurityUtils.INSTANCE.canAccess(player, entity);
    }
}