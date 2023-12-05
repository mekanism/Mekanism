package mekanism.common.inventory.container.entity.robit;

import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class RepairRobitContainer extends AnvilMenu implements IEntityContainer<EntityRobit>, ISecurityContainer {

    private final EntityRobit entity;

    public RepairRobitContainer(int id, Inventory inv, EntityRobit robit) {
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
        return MekanismContainerTypes.REPAIR_ROBIT.get();
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        entity.close(player);
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return IEntitySecurityUtils.INSTANCE.canAccess(player, entity);
    }
}