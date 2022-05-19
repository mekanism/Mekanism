package mekanism.client.render;

import javax.annotation.Nonnull;
import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.item.block.RenderChemicalDissolutionChamberItem;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.block.RenderIndustrialAlarmItem;
import mekanism.client.render.item.block.RenderQuantumEntangloporterItem;
import mekanism.client.render.item.block.RenderSeismicVibratorItem;
import mekanism.client.render.item.block.RenderSolarNeutronActivatorItem;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.common.block.BlockBounding;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.client.IItemRenderProperties;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class RenderPropertiesProvider {

    private RenderPropertiesProvider() {
    }

    public static IItemRenderProperties energyCube() {
        return new MekRenderProperties(RenderEnergyCubeItem.RENDERER);
    }

    public static IItemRenderProperties dissolution() {
        return new MekRenderProperties(RenderChemicalDissolutionChamberItem.RENDERER);
    }

    public static IItemRenderProperties fluidTank() {
        return new MekRenderProperties(RenderFluidTankItem.RENDERER);
    }

    public static IItemRenderProperties industrialAlarm() {
        return new MekRenderProperties(RenderIndustrialAlarmItem.RENDERER);
    }

    public static IItemRenderProperties entangloporter() {
        return new MekRenderProperties(RenderQuantumEntangloporterItem.RENDERER);
    }

    public static IItemRenderProperties seismicVibrator() {
        return new MekRenderProperties(RenderSeismicVibratorItem.RENDERER);
    }

    public static IItemRenderProperties activator() {
        return new MekRenderProperties(RenderSolarNeutronActivatorItem.RENDERER);
    }

    public static IItemRenderProperties armoredJetpack() {
        return new MekCustomArmorRenderProperties(RenderJetpack.ARMORED_RENDERER, JetpackArmor.ARMORED_JETPACK);
    }

    public static IItemRenderProperties jetpack() {
        return new MekCustomArmorRenderProperties(RenderJetpack.RENDERER, JetpackArmor.JETPACK);
    }

    public static IItemRenderProperties disassembler() {
        return new MekRenderProperties(RenderAtomicDisassembler.RENDERER);
    }

    public static IItemRenderProperties flamethrower() {
        return new MekRenderProperties(RenderFlameThrower.RENDERER);
    }

    public static IItemRenderProperties armoredFreeRunners() {
        return new MekCustomArmorRenderProperties(RenderFreeRunners.ARMORED_RENDERER, FreeRunnerArmor.ARMORED_FREE_RUNNERS);
    }

    public static IItemRenderProperties freeRunners() {
        return new MekCustomArmorRenderProperties(RenderFreeRunners.RENDERER, FreeRunnerArmor.FREE_RUNNERS);
    }

    public static IItemRenderProperties scubaMask() {
        return new MekCustomArmorRenderProperties(RenderScubaMask.RENDERER, ScubaMaskArmor.SCUBA_MASK);
    }

    public static IItemRenderProperties scubaTank() {
        return new MekCustomArmorRenderProperties(RenderScubaTank.RENDERER, ScubaTankArmor.SCUBA_TANK);
    }

    public static IItemRenderProperties mekaSuit() {
        return MEKA_SUIT;
    }

    private static final IItemRenderProperties MEKA_SUIT = new ISpecialGear() {
        @Nonnull
        @Override
        public ICustomArmor getGearModel(EquipmentSlot slot) {
            return switch (slot) {
                case HEAD -> MekaSuitArmor.HELMET;
                case CHEST -> MekaSuitArmor.BODYARMOR;
                case LEGS -> MekaSuitArmor.PANTS;
                default -> MekaSuitArmor.BOOTS;
            };
        }
    };

    public static IBlockRenderProperties particles() {
        return PARTICLE_HANDLER;
    }

    public static IBlockRenderProperties boundingParticles() {
        return new IBlockRenderProperties() {
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

    public record MekRenderProperties(BlockEntityWithoutLevelRenderer renderer) implements IItemRenderProperties {

        @Override
        public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
            return renderer;
        }
    }

    public record MekCustomArmorRenderProperties(BlockEntityWithoutLevelRenderer renderer, ICustomArmor gearModel) implements ISpecialGear {

        @Override
        public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
            return renderer;
        }

        @Nonnull
        @Override
        public ICustomArmor getGearModel(EquipmentSlot slot) {
            return gearModel;
        }
    }

    private static final IBlockRenderProperties PARTICLE_HANDLER = new IBlockRenderProperties() {
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