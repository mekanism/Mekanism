package mekanism.common.content.gear.mekasuit;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

@ParametersAreNonnullByDefault
public class ModuleHydraulicPropulsionUnit implements ICustomModule<ModuleHydraulicPropulsionUnit> {

    private IModuleConfigItem<JumpBoost> jumpBoost;
    private IModuleConfigItem<StepAssist> stepAssist;

    @Override
    public void init(IModule<ModuleHydraulicPropulsionUnit> module, ModuleConfigItemCreator configItemCreator) {
        jumpBoost = configItemCreator.createConfigItem("jump_boost", MekanismLang.MODULE_JUMP_BOOST,
              new ModuleEnumData<>(JumpBoost.class, module.getInstalledCount() + 1, JumpBoost.LOW));
        stepAssist = configItemCreator.createConfigItem("step_assist", MekanismLang.MODULE_STEP_ASSIST,
              new ModuleEnumData<>(StepAssist.class, module.getInstalledCount() + 1, StepAssist.LOW));
    }

    public float getBoost() {
        return jumpBoost.get().getBoost();
    }

    public float getStepHeight() {
        return stepAssist.get().getHeight();
    }

    public enum JumpBoost implements IHasTextComponent {
        OFF(0),
        LOW(0.5F),
        MED(1),
        HIGH(3),
        ULTRA(5);

        private final float boost;
        private final ITextComponent label;

        JumpBoost(float boost) {
            this.boost = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public float getBoost() {
            return boost;
        }
    }

    public enum StepAssist implements IHasTextComponent {
        OFF(0),
        LOW(0.5F),
        MED(1),
        HIGH(1.5F),
        ULTRA(2);

        private final float height;
        private final ITextComponent label;

        StepAssist(float height) {
            this.height = height;
            this.label = TextComponentUtil.getString(Float.toString(height));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public float getHeight() {
            return height;
        }
    }
}