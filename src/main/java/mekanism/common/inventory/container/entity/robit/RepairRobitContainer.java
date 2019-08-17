package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class RepairRobitContainer extends RepairContainer implements INamedContainerProvider, IEntityContainer<EntityRobit> {

    private EntityRobit entity;

    public RepairRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(id, inv);
        this.entity = robit;
    }

    public RepairRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, MekanismEntityContainer.getEntityFromBuf(buf, EntityRobit.class));
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    @Override
    public EntityRobit getEntity() {
        return entity;
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new RepairRobitContainer(i, inv, entity);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.robit.repair");
    }
}