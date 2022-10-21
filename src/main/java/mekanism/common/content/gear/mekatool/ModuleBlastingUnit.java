package mekanism.common.content.gear.mekatool;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@ParametersAreNotNullByDefault
public class ModuleBlastingUnit implements ICustomModule<ModuleBlastingUnit> {

    private IModuleConfigItem<BlastRadius> blastRadius;

    @Override
    public void init(IModule<ModuleBlastingUnit> module, ModuleConfigItemCreator configItemCreator) {
        blastRadius = configItemCreator.createConfigItem("blast_radius", MekanismLang.MODULE_BLAST_RADIUS,
                new ModuleEnumData<>(BlastRadius.LOW, module.getInstalledCount() + 1));
    }

    public int getBlastRadius() {
        return blastRadius.get().getRadius();
    }

    @Override
    public void addHUDStrings(IModule<ModuleBlastingUnit> module, Player player, Consumer<Component> hudStringAdder) {
        //Only add hud string if enabled in config
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_BLASTING_ENABLED.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, blastRadius.get()));
        }
    }

    @NothingNullByDefault
    public enum BlastRadius implements IHasTextComponent {
        OFF(0),
        LOW(1),
        MED(2),
        HIGH(3),
        EXTREME(4);

        private final int radius;
        private final Component label;

        BlastRadius(int radius) {
            this.radius = radius;
            this.label = MekanismLang.MODULE_BLAST_AREA.translate(2 * radius + 1);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getRadius() {
            return radius;
        }
    }
}