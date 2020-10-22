package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return entity instanceof ISecurityObject ? (ISecurityObject) entity : ISecurityObject.NO_SECURITY;
    }

    public static <ENTITY extends Entity> ENTITY getEntityFromBuf(PacketBuffer buf, Class<ENTITY> type) {
        if (buf == null) {
            return null;
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(buf.readVarInt());
            if (type.isInstance(entity)) {
                return (ENTITY) entity;
            }
            return null;
        });
    }
}