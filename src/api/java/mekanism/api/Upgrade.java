package mekanism.api;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public enum Upgrade implements IHasTranslationKey {
    SPEED("speed", 8, EnumColor.RED),
    ENERGY("energy", 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", 1, EnumColor.DARK_AQUA),
    GAS("gas", 8, EnumColor.YELLOW),
    MUFFLING("muffling", 4, EnumColor.DARK_GRAY),
    ANCHOR("anchor", 1, EnumColor.DARK_GREEN);

    private String name;
    private int maxStack;
    private EnumColor color;

    Upgrade(String s, int max, EnumColor c) {
        name = s;
        maxStack = max;
        color = c;
    }

    public static Map<Upgrade, Integer> buildMap(@Nullable CompoundNBT nbtTags) {
        Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
        if (nbtTags != null) {
            if (nbtTags.contains("upgrades")) {
                ListNBT list = nbtTags.getList("upgrades", NBT.TAG_COMPOUND);
                for (int tagCount = 0; tagCount < list.size(); tagCount++) {
                    CompoundNBT compound = list.getCompound(tagCount);
                    Upgrade upgrade = values()[compound.getInt("type")];
                    upgrades.put(upgrade, compound.getInt("amount"));
                }
            }
        }
        return upgrades;
    }

    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(getTagFor(entry.getKey(), entry.getValue()));
        }
        nbtTags.put("upgrades", list);
    }

    public static CompoundNBT getTagFor(Upgrade upgrade, int amount) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("type", upgrade.ordinal());
        compound.putInt("amount", amount);
        return compound;
    }

    public String getRawName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return "upgrade.mekanism." + name;
    }

    public String getDescription() {
        return "upgrade.mekanism." + name + ".desc";
    }

    public int getMax() {
        return maxStack;
    }

    public EnumColor getColor() {
        return color;
    }

    public boolean canMultiply() {
        return getMax() > 1;
    }

    public interface IUpgradeInfoHandler {

        List<ITextComponent> getInfo(Upgrade upgrade);
    }
}