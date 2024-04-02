package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPortalFX(BlockPos pos, Direction direction) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("portal_fx");

    public PacketPortalFX(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(Direction.class));
    }

    public PacketPortalFX(BlockPos pos) {
        this(pos, Direction.UP);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Level world = context.level().orElse(null);
        if (world != null) {
            BlockPos secondPos = pos.relative(direction);
            for (int i = 0; i < 50; i++) {
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.random.nextFloat(), pos.getY() + world.random.nextFloat(),
                      pos.getZ() + world.random.nextFloat(), 0.0F, 0.0F, 0.0F);
                world.addParticle(ParticleTypes.PORTAL, secondPos.getX() + world.random.nextFloat(), secondPos.getY() + world.random.nextFloat(),
                      secondPos.getZ() + world.random.nextFloat(), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(direction);
    }
}