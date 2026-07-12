package mekanism.common.integration.gender;

import com.wildfire.api.IGenderArmor;
import mekanism.common.Mekanism;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

public record MekanismGenderArmor(boolean coversBreasts, boolean alwaysHidesBreasts, float physicsResistance, float tightness, boolean armorStandsCopySettings) implements IGenderArmor {

    private static final ItemCapability<IGenderArmor, @Nullable Void> GENDER_ARMOR_CAPABILITY = ItemCapability.createVoid(Mekanism.hooks.genderMod.rl("gender_armor"), IGenderArmor.class);

    public MekanismGenderArmor(float physicsResistance) {
        this(physicsResistance, 0);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness) {
        this(physicsResistance, tightness, true);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness, boolean armorStandsCopySettings) {
        this(true, false, physicsResistance, tightness, armorStandsCopySettings);
    }

    public MekanismGenderArmor {
        if (physicsResistance < 0 || physicsResistance > 1) {
            throw new IllegalArgumentException("Physics resistance must be between zero and one inclusive.");
        } else if (tightness < 0 || tightness > 1) {
            throw new IllegalArgumentException("Armor tightness must be between zero and one inclusive.");
        }
    }

    public static void register(RegisterCapabilitiesEvent event, MekanismGenderArmor armor, ItemLike... items) {
        event.registerItem(GENDER_ARMOR_CAPABILITY, (_, _) -> armor, items);
    }
}