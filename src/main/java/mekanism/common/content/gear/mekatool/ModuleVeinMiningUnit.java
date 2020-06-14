package mekanism.common.content.gear.mekatool;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ModuleVeinMiningUnit extends ModuleMekaTool {

    private ModuleConfigItem<Boolean> extendedMode;
    private ModuleConfigItem<ExcavationRange> excavationRange;

    @Override
    public void init() {
        super.init();
        addConfigItem(extendedMode = new ModuleConfigItem<>(this, "extended_mode", MekanismLang.MODULE_EXTENDED_MODE, new BooleanData(), false));
        addConfigItem(excavationRange = new ModuleConfigItem<>(this, "excavation_range", MekanismLang.MODULE_EXCAVATION_RANGE, new EnumData<>(ExcavationRange.class, getInstalledCount() + 1), ExcavationRange.LOW));
    }

    public boolean isExtended() {
        return extendedMode.get();
    }

    public int getExcavationRange() {
        return excavationRange.get().getRange();
    }

    public static Set<BlockPos> findPositions(PlayerEntity player, BlockState state, BlockPos location, World world, int maxRange) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Set<BlockPos> openSet = new LinkedHashSet<>();
        openSet.add(location);
        Block startBlock = state.getBlock();
        int maxCount = MekanismConfig.gear.disassemblerMiningCount.get() - 1;
        while (!openSet.isEmpty()) {
            BlockPos blockPos = openSet.iterator().next();
            found.add(blockPos);
            openSet.remove(blockPos);
            if (found.size() > maxCount) {
                return found;
            }
            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
                //We can check contains as mutable
                if (!found.contains(pos) && (maxRange == -1 || MekanismUtils.distanceBetween(location, pos) <= maxRange)) {
                    if (world.isBlockPresent(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                        //Make sure to add it as immutable
                        if (!openSet.contains(pos)) {
                            openSet.add(pos.toImmutable());
                            BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vec3d(blockPos).add(0.5, 0.5, 0.5), new Vec3d(pos).add(0.5, 0.5, 0.5), 10)
                                  .size(0.015F).lifespan(12).spawn(SpawnFunction.NO_DELAY);
                            Mekanism.proxy.renderBolt(Objects.hash(blockPos, pos), bolt);
                        }
                    }
                }
            }
        }
        return found;
    }

    public enum ExcavationRange implements IHasTextComponent {
        OFF(0),
        LOW(2),
        MED(4),
        HIGH(6),
        EXTREME(8);

        private final int range;
        private final ITextComponent label;

        ExcavationRange(int range) {
            this.range = range;
            this.label = new StringTextComponent(Integer.toString(range));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getRange() {
            return range;
        }
    }
}
