package mekanism.common.content.gear.mekatool;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
public class ModuleVeinMiningUnit implements ICustomModule<ModuleVeinMiningUnit> {

    private IModuleConfigItem<Boolean> extendedMode;
    private IModuleConfigItem<ExcavationRange> excavationRange;

    @Override
    public void init(IModule<ModuleVeinMiningUnit> module, ModuleConfigItemCreator configItemCreator) {
        extendedMode = configItemCreator.createDisableableConfigItem("extended_mode", MekanismLang.MODULE_EXTENDED_MODE, false, MekanismConfig.gear.mekaToolExtendedMining);
        excavationRange = configItemCreator.createConfigItem("excavation_range", MekanismLang.MODULE_EXCAVATION_RANGE,
              new ModuleEnumData<>(ExcavationRange.class, module.getInstalledCount() + 1, ExcavationRange.LOW));
    }

    public boolean isExtended() {
        return extendedMode.get();
    }

    public int getExcavationRange() {
        return excavationRange.get().getRange();
    }

    public static Set<BlockPos> findPositions(BlockState state, BlockPos location, World world, int maxRange) {
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
            for (BlockPos pos : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
                //We can check contains as mutable
                if (!found.contains(pos) && (maxRange == -1 || WorldUtils.distanceBetween(location, pos) <= maxRange)) {
                    Optional<BlockState> blockState = WorldUtils.getBlockState(world, pos);
                    if (blockState.isPresent() && startBlock == blockState.get().getBlock()) {
                        //Make sure to add it as immutable
                        if (openSet.add(pos.immutable())) {
                            //Note: We do this for all blocks we find/attempt to mine, not just ones we do mine, as it is a bit simpler
                            // and also represents those blocks getting checked by the vein mining for potentially being able to be mined
                            Mekanism.packetHandler.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(blockPos, pos),
                                  Vector3d.atCenterOf(blockPos), Vector3d.atCenterOf(pos), 10), world, blockPos);
                        }
                    }
                }
            }
        }
        return found;
    }

    @Override
    public void addHUDStrings(IModule<ModuleVeinMiningUnit> module, PlayerEntity player, Consumer<ITextComponent> hudStringAdder) {
        //Only add hud string for extended vein mining if enabled in config
        if (module.isEnabled() && MekanismConfig.gear.mekaToolExtendedMining.getAsBoolean()) {
            hudStringAdder.accept(MekanismLang.MODULE_EXTENDED_ENABLED.translateColored(EnumColor.DARK_GRAY,
                  isExtended() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED,
                  isExtended() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER));
        }
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
            this.label = TextComponentUtil.getString(Integer.toString(range));
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
