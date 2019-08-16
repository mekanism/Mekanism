package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer {

    protected final ENTITY entity;

    protected MekanismEntityContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
    }

    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    public static <ENTITY extends Entity> ENTITY getEntityFromBuf(PacketBuffer buf, Class<ENTITY> type) {
        if (buf == null) {
            return null;
        }
        //TODO: Handle it being client side only better?
        return DistExecutor.runForDist(() -> () -> {
            //TODO: Implement this
            Entity entity = null;
            if (type.isInstance(entity)) {
                return (ENTITY) entity;
            }
            return null;
        }, () -> () -> {
            throw new RuntimeException("Shouldn't be called on server!");
        });
    }
}