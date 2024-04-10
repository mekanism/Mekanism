package mekanism.common.util.text;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public abstract class BooleanStateDisplay implements IHasTextComponent {

    protected final boolean value;
    protected final boolean colored;

    protected BooleanStateDisplay(boolean value, boolean colored) {
        this.value = value;
        this.colored = colored;
    }

    protected abstract ILangEntry getLangEntry();

    @Override
    public Component getTextComponent() {
        if (colored) {
            return getLangEntry().translateColored(value ? EnumColor.BRIGHT_GREEN : EnumColor.RED);
        }
        return getLangEntry().translate();
    }

    public static class YesNo extends BooleanStateDisplay {

        public static final YesNo YES = new YesNo(true, false);
        public static final YesNo NO = new YesNo(false, false);
        public static final YesNo YES_COLORED = new YesNo(true, true);
        public static final YesNo NO_COLORED = new YesNo(false, true);

        private YesNo(boolean value, boolean colored) {
            super(value, colored);
        }

        public static YesNo of(boolean value) {
            return value ? YES : NO;
        }

        public static YesNo hasInventory(ItemStack stack) {
            return of(ContainerType.ITEM.getAttachmentContainersIfPresent(stack).stream().anyMatch(slot -> !slot.isEmpty()), true);
        }

        public static YesNo of(boolean value, boolean colored) {
            if (colored) {
                return value ? YES_COLORED : NO_COLORED;
            }
            return of(value);
        }

        @Override
        protected ILangEntry getLangEntry() {
            return value ? MekanismLang.YES : MekanismLang.NO;
        }
    }

    public static class OnOff extends BooleanStateDisplay {

        public static final OnOff ON = new OnOff(true, false, false);
        public static final OnOff OFF = new OnOff(false, false, false);
        public static final OnOff ON_COLORED = new OnOff(true, true, false);
        public static final OnOff OFF_COLORED = new OnOff(false, true, false);

        private final boolean caps;

        private OnOff(boolean value, boolean colored, boolean caps) {
            super(value, colored);
            this.caps = caps;
        }

        public static OnOff of(boolean value) {
            return value ? ON : OFF;
        }

        public static OnOff of(boolean value, boolean colored) {
            if (colored) {
                return value ? ON_COLORED : OFF_COLORED;
            }
            return of(value);
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