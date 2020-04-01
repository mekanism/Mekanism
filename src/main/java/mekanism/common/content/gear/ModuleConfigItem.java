package mekanism.common.content.gear;

import mekanism.common.base.ILangEntry;
import net.minecraft.nbt.CompoundNBT;

public class ModuleConfigItem<TYPE> {

    private Module module;
    private String name;
    private ILangEntry description;
    private ConfigData<TYPE> data;

    public ModuleConfigItem(Module module, String name, ILangEntry description, ConfigData<TYPE> data) {
        this.module = module;
        this.name = name;
        this.description = description;
        this.data = data;
    }

    public ILangEntry getDescription() {
        return description;
    }

    public ConfigData<TYPE> getData() {
        return data;
    }

    public TYPE get() {
        return data.get();
    }

    public void set(TYPE type, Runnable callback) {
        data.set(type);
        module.save(callback);
    }

    public void read(CompoundNBT tag) {
        data.read(name, tag);
    }

    public void write(CompoundNBT tag) {
        data.write(name, tag);
    }

    private interface ConfigData<TYPE> {
        TYPE get();
        void set(TYPE val);
        void read(String name, CompoundNBT tag);
        void write(String name, CompoundNBT tag);
    }

    public static class BooleanData implements ConfigData<Boolean> {

        private boolean value;

        public BooleanData() {
            this(true);
        }

        public BooleanData(boolean def) {
            value = def;
        }

        @Override
        public Boolean get() {
            return value;
        }

        @Override
        public void set(Boolean val) {
            value = val;
        }

        @Override
        public void read(String name, CompoundNBT tag) {
            value = tag.getBoolean(name);
        }

        @Override
        public void write(String name, CompoundNBT tag) {
            tag.putBoolean(name, value);
        }
    }

    public static interface IntEnum {
        public int getValue();
    }

    public static class EnumData implements ConfigData<Enum<? extends IntEnum>> {

        private Class<Enum<? extends IntEnum>> enumClass;
        private Enum<? extends IntEnum> value;

        public EnumData(Class<Enum<? extends IntEnum>> enumClass, Enum<? extends IntEnum> def) {
            this.enumClass = enumClass;
            value = def;
        }

        @Override
        public Enum<? extends IntEnum> get() {
            return value;
        }

        @Override
        public void set(Enum<? extends IntEnum> val) {
            value = val;
        }

        @Override
        public void read(String name, CompoundNBT tag) {
            value = enumClass.getEnumConstants()[tag.getInt(name)];
        }

        @Override
        public void write(String name, CompoundNBT tag) {
            tag.putInt(name, value.ordinal());
        }

        public Class<Enum<? extends IntEnum>> getEnumClass() {
            return enumClass;
        }
    }
}
