/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import java.util.Locale;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum EnumColor {

	BLACK,
	RED,
	GREEN,
	BROWN,
	BLUE,
	PURPLE,
	CYAN,
	LIGHT_GRAY,
	GRAY,
	PINK,
	LIME,
	YELLOW,
	LIGHT_BLUE,
	MAGENTA,
	ORANGE,
	WHITE;
	public static final EnumColor[] VALUES = values();
	public static final String[] DYES = {
		"dyeBlack",
		"dyeRed",
		"dyeGreen",
		"dyeBrown",
		"dyeBlue",
		"dyePurple",
		"dyeCyan",
		"dyeLightGray",
		"dyeGray",
		"dyePink",
		"dyeLime",
		"dyeYellow",
		"dyeLightBlue",
		"dyeMagenta",
		"dyeOrange",
		"dyeWhite"};
	public static final String[] NAMES = {
		"Black",
		"Red",
		"Green",
		"Brown",
		"Blue",
		"Purple",
		"Cyan",
		"LightGray",
		"Gray",
		"Pink",
		"Lime",
		"Yellow",
		"LightBlue",
		"Magenta",
		"Orange",
		"White"};
	public static final int[] DARK_HEX = {
		0x2D2D2D,
		0xA33835,
		0x394C1E,
		0x5C3A24,
		0x3441A2,
		0x843FBF,
		0x36809E,
		0x888888,
		0x444444,
		0xE585A0,
		0x3FAA36,
		0xCFC231,
		0x7F9AD1,
		0xFF64FF,
		0xFF6A00,
		0xFFFFFF};
	public static final int[] LIGHT_HEX = {
		0x181414,
		0xBE2B27,
		0x007F0E,
		0x89502D,
		0x253193,
		0x7e34bf,
		0x299799,
		0xa0a7a7,
		0x7A7A7A,
		0xD97199,
		0x39D52E,
		0xFFD91C,
		0x66AAFF,
		0xD943C6,
		0xEA7835,
		0xe4e4e4};

	@SideOnly(Side.CLIENT)
	private static IIcon[] brushIcons;

	public int getDarkHex() {
		return DARK_HEX[ordinal()];
	}

	public int getLightHex() {
		return LIGHT_HEX[ordinal()];
	}

	public static EnumColor fromId(int id) {
		if (id < 0 || id >= VALUES.length) {
			return WHITE;
		}
		return VALUES[id];
	}

	public static EnumColor fromDye(String dyeTag) {
		for (int id = 0; id < DYES.length; id++) {
			if (DYES[id].equals(dyeTag)) {
				return VALUES[id];
			}
		}
		return null;
	}

	public static EnumColor fromName(String name) {
		for (int id = 0; id < NAMES.length; id++) {
			if (NAMES[id].equals(name)) {
				return VALUES[id];
			}
		}
		return null;
	}

	public static EnumColor getRand() {
		return VALUES[new Random().nextInt(VALUES.length)];
	}

	public EnumColor getNext() {
		EnumColor next = VALUES[(ordinal() + 1) % VALUES.length];
		return next;
	}

	public EnumColor getPrevious() {
		EnumColor previous = VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
		return previous;
	}

	public EnumColor inverse() {
		return EnumColor.VALUES[15 - ordinal()];
	}

	public String getTag() {
		return "color." + name().replace("_", ".").toLowerCase(Locale.ENGLISH);
	}

	public String getBasicTag() {
		return name().replace("_", ".").toLowerCase(Locale.ENGLISH);
	}

	public String getName() {
		return NAMES[ordinal()];
	}

	public String getLocalizedName() {
		return StatCollector.translateToLocal(getTag());
	}

	public String getDye() {
		return DYES[ordinal()];
	}

	@Override
	public String toString() {
		String s = name().replace("_", " ");
		String[] words = s.split(" ");
		StringBuilder b = new StringBuilder();
		for (String word : words) {
			b.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.ENGLISH)).append(" ");
		}
		return b.toString().trim();
	}

	@SideOnly(Side.CLIENT)
	public static void registerIcons(IIconRegister iconRegister) {
		brushIcons = new IIcon[16];
		for (EnumColor c : values()) {
			brushIcons[c.ordinal()] = iconRegister.registerIcon("buildcraft:triggers/color_"
					+ c.name().toLowerCase(Locale.ENGLISH));
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return brushIcons [ordinal()];
	}
}
