package mekanism.client.render;

import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.block.BlockBounding;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class RenderPropertiesProvider {

    private RenderPropertiesProvider() {
    }

    public static IClientBlockExtensions boundingParticles() {
        return new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
                if (target.getType() == Type.BLOCK && target instanceof BlockHitResult blockTarget) {
                    BlockPos pos = blockTarget.getBlockPos();
                    BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
                    if (mainPos != null) {
                        BlockState mainState = world.getBlockState(mainPos);
                        if (!mainState.isAir()) {
                            //Copy of ParticleManager#addBlockHitEffects except using the block state for the main position
                            AABB axisalignedbb = state.getShape(world, pos).bounds();
                            double x = pos.getX() + world.random.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2) + 0.1 + axisalignedbb.minX;
                            double y = pos.getY() + world.random.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2) + 0.1 + axisalignedbb.minY;
                            double z = pos.getZ() + world.random.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2) + 0.1 + axisalignedbb.minZ;
                            Direction side = blockTarget.getDirection();
                            switch (side) {
                                case DOWN -> y = pos.getY() + axisalignedbb.minY - 0.1;
                                case UP -> y = pos.getY() + axisalignedbb.maxY + 0.1;
                                case NORTH -> z = pos.getZ() + axisalignedbb.minZ - 0.1;
                                case SOUTH -> z = pos.getZ() + axisalignedbb.maxZ + 0.1;
                                case WEST -> x = pos.getX() + axisalignedbb.minX - 0.1;
                                case EAST -> x = pos.getX() + axisalignedbb.maxX + 0.1;
                            }
                            manager.add(new TerrainParticle((ClientLevel) world, x, y, z, 0, 0, 0, mainState)
                                  .updateSprite(mainState, mainPos).setPower(0.2F).scale(0.6F));
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public record MekRenderProperties(BlockEntityWithoutLevelRenderer renderer) implements IClientItemExtensions {

        @NotNull
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    }

    public record MekCustomArmorRenderProperties(BlockEntityWithoutLevelRenderer renderer, ICustomArmor gearModel) implements ISpecialGear {

        @NotNull
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    }

    public static final IClientBlockExtensions PARTICLE_HANDLER = new IClientBlockExtensions() {
        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
            //Copy of ParticleManager#addBlockDestroyEffects, but removes the minimum number of particles each voxel shape produces
            state.getShape(Level, pos).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double xDif = Math.min(1, maxX - minX);
                double yDif = Math.min(1, maxY - minY);
                double zDif = Math.min(1, maxZ - minZ);
                //Don't force the counts to be at least two
                int xCount = Mth.ceil(xDif / 0.25);
                int yCount = Mth.ceil(yDif / 0.25);
                int zCount = Mth.ceil(zDif / 0.25);
                if (xCount > 0 && yCount > 0 && zCount > 0) {
                    for (int x = 0; x < xCount; x++) {
                        for (int y = 0; y < yCount; y++) {
                            for (int z = 0; z < zCount; z++) {
                                double d4 = (x + 0.5) / xCount;
                                double d5 = (y + 0.5) / yCount;
                                double d6 = (z + 0.5) / zCount;
                                double d7 = d4 * xDif + minX;
                                double d8 = d5 * yDif + minY;
                                double d9 = d6 * zDif + minZ;
                                manager.add(new TerrainParticle((ClientLevel) Level, pos.getX() + d7, pos.getY() + d8,
                                      pos.getZ() + d9, d4 - 0.5, d5 - 0.5, d6 - 0.5, state).updateSprite(state, pos));
                            }
                        }
                    }
                }
            });
            return true;
        }
    };
}