package mekanism.common.util.text;

import mekanism.api.text.IHasTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public abstract class BooleanStateDisplay implements IHasTextComponent {

    protected final boolean value;
    protected final boolean colored;

    protected BooleanStateDisplay(boolean value, boolean colored) {
        this.value = value;
        this.colored = colored;
    }

    protected abstract String getKey();

    @Override
    public ITextComponent getTextComponent() {
        ITextComponent translation = TextComponentUtil.translate(getKey());
        if (colored) {
            translation.applyTextStyle(value ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
        }
        return translation;
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
        protected String getKey() {
            return "tooltip.mekanism." + (value ? "yes" : "no");
        }
    }

    public static class OnOff extends BooleanStateDisplay {

        private OnOff(boolean value, boolean colored) {
            super(value, colored);
        }

        public static OnOff of(boolean value) {
            return of(value, false);
        }

        public static OnOff of(boolean value, boolean colored) {
            return new OnOff(value, colored);
        }

        @Override
        protected String getKey() {
            return "gui.mekanism." + (value ? "on" : "off");
        }
    }

    public static class OutputInput extends BooleanStateDisplay {

        private OutputInput(boolean value, boolean colored) {
            super(value, colored);
        }

        public static OutputInput of(boolean value) {
            return of(value, false);
        }

        public static OutputInput of(boolean value, boolean colored) {
            return new OutputInput(value, colored);
        }

        @Override
        protected String getKey() {
            return "gui.mekanism." + (value ? "output" : "input");
        }
    }
}