package mekanism.common.util.text;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public abstract class BooleanStateDisplay implements IHasTextComponent {

    protected final boolean value;
    protected final boolean colored;

    protected BooleanStateDisplay(boolean value, boolean colored) {
        this.value = value;
        this.colored = colored;
    }

    protected abstract ILangEntry getLangEntry();

    @Override
    public ITextComponent getTextComponent() {
        if (colored) {
            return getLangEntry().translateColored(value ? EnumColor.BRIGHT_GREEN : EnumColor.RED);
        }
        return getLangEntry().translate();
    }

    public static class YesNo extends BooleanStateDisplay {

        private YesNo(boolean value, boolean colored) {
            super(value, colored);
        }

        public static YesNo of(boolean value) {
            return of(value, false);
        }

        public static YesNo of(boolean value, boolean colored) {
            return new YesNo(value, colored);
        }

        @Override
        protected ILangEntry getLangEntry() {
            return value ? MekanismLang.YES : MekanismLang.NO;
        }
    }

    public static class OnOff extends BooleanStateDisplay {

        private final boolean caps;

        private OnOff(boolean value, boolean colored, boolean caps) {
            super(value, colored);
            this.caps = caps;
        }

        public static OnOff of(boolean value) {
            return of(value, false);
        }

        public static OnOff of(boolean value, boolean colored) {
            return new OnOff(value, colored, false);
        }

        public static OnOff caps(boolean value, boolean colored) {
            return new OnOff(value, colored, true);
        }

        @Override
        protected ILangEntry getLangEntry() {
            return value ? (caps ? MekanismLang.ON_CAPS : MekanismLang.ON) : (caps ? MekanismLang.OFF_CAPS : MekanismLang.OFF);
        }
    }

    public static class InputOutput extends BooleanStateDisplay {

        private InputOutput(boolean value, boolean colored) {
            super(value, colored);
        }

        public static InputOutput of(boolean value) {
            return of(value, false);
        }

        public static InputOutput of(boolean value, boolean colored) {
            return new InputOutput(value, colored);
        }

        @Override
        protected ILangEntry getLangEntry() {
            return value ? MekanismLang.INPUT : MekanismLang.OUTPUT;
        }
    }

    public static class ActiveDisabled extends BooleanStateDisplay {

        private ActiveDisabled(boolean value, boolean colored) {
            super(value, colored);
        }

        public static ActiveDisabled of(boolean value) {
            return of(value, false);
        }

        public static ActiveDisabled of(boolean value, boolean colored) {
            return new ActiveDisabled(value, colored);
        }

        @Override
        protected ILangEntry getLangEntry() {
            return value ? MekanismLang.ACTIVE : MekanismLang.DISABLED;
        }
    }
}