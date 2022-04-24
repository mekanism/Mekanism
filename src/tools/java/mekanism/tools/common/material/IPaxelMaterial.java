package mekanism.tools.common.material;

public interface IPaxelMaterial {

    float getPaxelDamage();

    default float getPaxelAtkSpeed() {
        return -2.4F;
    }

    int getPaxelMaxUses();

    float getPaxelEfficiency();

    int getPaxelEnchantability();

    String getConfigCommentName();
}