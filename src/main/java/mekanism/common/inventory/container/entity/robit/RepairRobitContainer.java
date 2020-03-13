package mekanism.common.inventory.container.entity.robit;

import java.util.Optional;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RepairRobitContainer extends RepairContainer implements IEntityContainer<EntityRobit> {

    private EntityRobit entity;

    public RepairRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(id, inv, new IWorldPosCallable() {
            @Nonnull
            @Override
            public <T> Optional<T> apply(@Nonnull BiFunction<World, BlockPos, T> worldBlockPosTBiFunction) {
                //Note: We use an anonymous class implementation rather than using IWorldPosCallable.of, so that if the robit moves
                // this uses the proper updated position
                return Optional.of(worldBlockPosTBiFunction.apply(robit.getEntityWorld(), robit.getPosition()));
            }
        });
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

    @Nonnull
    @Override
    public ContainerType<?> getType() {
        return MekanismContainerTypes.REPAIR_ROBIT.getContainerType();
    }
}