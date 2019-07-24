package mekanism.tools.common;

import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.MekanismItems;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.ToolsConfig;
import mekanism.common.config.ToolsConfig.ArmorBalance;
import mekanism.common.config.ToolsConfig.ToolBalance;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;

public enum Materials {
    OBSIDIAN("OBSIDIAN", cfg -> cfg.toolOBSIDIAN, cfg -> cfg.toolOBSIDIAN2, cfg -> cfg.armorOBSIDIAN, () -> new ItemStack(MekanismItems.Ingot)),
    LAZULI("LAZULI", cfg -> cfg.toolLAZULI, cfg -> cfg.toolLAZULI2, cfg -> cfg.armorLAZULI, () -> new ItemStack(Items.DYE, 1, 4), SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND),
    OSMIUM("OSMIUM", cfg -> cfg.toolOSMIUM, cfg -> cfg.toolOSMIUM2, cfg -> cfg.armorOSMIUM, () -> new ItemStack(MekanismItems.Ingot, 1, 1)),
    BRONZE("BRONZE", cfg -> cfg.toolBRONZE, cfg -> cfg.toolBRONZE2, cfg -> cfg.armorBRONZE, () -> new ItemStack(MekanismItems.Ingot, 1, 2)),
    GLOWSTONE("GLOWSTONE", tools -> tools.toolGLOWSTONE, tools -> tools.toolGLOWSTONE2, cfg -> cfg.armorGLOWSTONE, () -> new ItemStack(MekanismItems.Ingot, 1, 3)),
    STEEL("STEEL", cfg -> cfg.toolSTEEL, cfg -> cfg.toolSTEEL2, cfg -> cfg.armorSTEEL, () -> new ItemStack(MekanismItems.Ingot, 1, 4));

    private final Function<ToolsConfig, ToolBalance> materialFunction;
    private final Function<ToolsConfig, ToolBalance> paxelMaterialFunction;
    private final Function<ToolsConfig, ArmorBalance> armorMaterialFunction;
    private final Supplier<ItemStack> repairStackSupplier;
    private final SoundEvent equipSound;
    private final String materialName;
    private ToolMaterial material;
    private ToolMaterial paxelMaterial;
    private ArmorMaterial armorMaterial;
    private boolean initialized;
    private float axeDamage;
    private float axeSpeed;

    Materials(String name, Function<ToolsConfig, ToolBalance> material, Function<ToolsConfig, ToolBalance> paxelMaterial, Function<ToolsConfig, ArmorBalance> armorMaterial,
          Supplier<ItemStack> repairStack) {
        this(name, material, paxelMaterial, armorMaterial, repairStack, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
    }

    Materials(String name, Function<ToolsConfig, ToolBalance> material, Function<ToolsConfig, ToolBalance> paxelMaterial, Function<ToolsConfig, ArmorBalance> armorMaterial,
          Supplier<ItemStack> repairStack, SoundEvent equipSound) {
        this.materialName = name;
        this.materialFunction = material;
        this.paxelMaterialFunction = paxelMaterial;
        this.armorMaterialFunction = armorMaterial;
        this.repairStackSupplier = repairStack;
        this.equipSound = equipSound;
    }

    private void init() {
        if (initialized) {
            return;
        }
        ToolBalance materialBalance = materialFunction.apply(MekanismConfig.current().tools);
        ToolBalance paxelMaterialBalance = paxelMaterialFunction.apply(MekanismConfig.current().tools);
        ArmorBalance armorBalance = armorMaterialFunction.apply(MekanismConfig.current().tools);

        this.material = getToolMaterial(materialName, materialBalance);
        this.paxelMaterial = getToolMaterial(materialName + "2", paxelMaterialBalance);
        this.armorMaterial = EnumHelper.addArmorMaterial(materialName, "TODO", armorBalance.durability.val(), new int[]{
              armorBalance.feetProtection.val(), armorBalance.legsProtection.val(), armorBalance.chestProtection.val(), armorBalance.headProtection.val(),
              }, armorBalance.enchantability.val(), equipSound, armorBalance.toughness.val());
        this.axeDamage = materialBalance.axeAttackDamage.val();
        this.axeSpeed = materialBalance.axeAttackSpeed.val();
        ItemStack repairStack = repairStackSupplier.get();
        material.setRepairItem(repairStack);
        paxelMaterial.setRepairItem(repairStack);
        armorMaterial.setRepairItem(repairStack);
        initialized = true;
    }

    public ToolMaterial getMaterial() {
        return material;
    }

    public ToolMaterial getPaxelMaterial() {
        return paxelMaterial;
    }

    public ArmorMaterial getArmorMaterial() {
        return armorMaterial;
    }

    public float getAxeDamage() {
        return axeDamage;
    }

    public float getAxeSpeed() {
        return axeSpeed;
    }

    private static ToolMaterial getToolMaterial(String enumName, ToolBalance config) {
        return EnumHelper.addToolMaterial(enumName, config.harvestLevel.val(), config.maxUses.val(), config.efficiency.val(), config.damage.val(), config.enchantability.val());
    }

    public static void load() {
        for (Materials material : values()) {
            material.init();
        }
    }
}