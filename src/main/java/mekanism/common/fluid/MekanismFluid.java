package mekanism.common.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class MekanismFluid extends BaseFlowingFluid {

    protected MekanismFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        super.entityInside(level, pos, entity, effectApplier);
        FluidState fluidState = level.getFluidState(pos);
        int temperature = getFluidType().getTemperature(fluidState, level, pos);
        if (temperature >= 1_300) {
            //If the temperature is as hot or hotter than lava, apply the same effects as lava would
            effectApplier.apply(InsideBlockEffectType.CLEAR_FREEZE);
            effectApplier.apply(InsideBlockEffectType.LAVA_IGNITE);
            effectApplier.runAfter(InsideBlockEffectType.LAVA_IGNITE, Entity::lavaHurt);
        } else if (temperature <= 273) {
            //Not technically in powdered snow but good enough
            effectApplier.apply(InsideBlockEffectType.FREEZE);
        }
    }

    public static class Flowing extends MekanismFluid {

        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, BlockStateProperties.MAX_LEVEL_8 - 1));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends MekanismFluid {

        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return BlockStateProperties.MAX_LEVEL_8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}