package mekanism.common.integration.gender;

import com.wildfire.api.IGenderArmor;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public record MekanismGenderArmor(boolean coversBreasts, boolean alwaysHidesBreasts, float physicsResistance, float tightness) implements IGenderArmor {

    private static final ItemCapability<IGenderArmor, Void> GENDER_ARMOR_CAPABILITY = ItemCapability.createVoid(new ResourceLocation(MekanismHooks.WILDFIRE_GENDER_MOD_ID, "gender_armor"), IGenderArmor.class);
    public static final MekanismGenderArmor OPEN_FRONT = new MekanismGenderArmor(false, false, 0, 0);
    public static final MekanismGenderArmor HIDES_BREASTS = new MekanismGenderArmor(true, true, 0, 0);
    static final MekanismGenderArmor HAZMAT = new MekanismGenderArmor(0.5F, 0.25F);

    public MekanismGenderArmor(float physicsResistance) {
        this(physicsResistance, 0);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness) {
        this(true, false, physicsResistance, tightness);
    }

    public MekanismGenderArmor {
        if (physicsResistance < 0 || physicsResistance > 1) {
            throw new IllegalArgumentException("Physics resistance must be between zero and one inclusive.");
        } else if (tightness < 0 || tightness > 1) {
            throw new IllegalArgumentException("Armor tightness must be between zero and one inclusive.");
        }
    }

    public void register(RegisterCapabilitiesEvent event, ArmorItem item) {
        event.registerItem(GENDER_ARMOR_CAPABILITY, (stack, ctx) -> this, item);
    }
}