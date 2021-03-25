package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
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

    @Nonnull
    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, @Nonnull ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @Nonnull
    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return entity instanceof ISecurityObject ? (ISecurityObject) entity : ISecurityObject.NO_SECURITY;
    }

    @Nonnull
    public static <ENTITY extends Entity> ENTITY getEntityFromBuf(PacketBuffer buf, Class<ENTITY> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().level == null) {
                throw new IllegalStateException("Client world is null.");
            }
            int entityId = buf.readVarInt();
            Entity e = Minecraft.getInstance().level.getEntity(entityId);
            if (type.isInstance(e)) {
                return (ENTITY) e;
            }
            throw new IllegalStateException("Client could not locate entity (id: " + entityId + ")  for entity container or the entity was of an invalid type. "
                                            + "This is likely caused by a mod breaking client side entity lookup.");
        });
    }
}