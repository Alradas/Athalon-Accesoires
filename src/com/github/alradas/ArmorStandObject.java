package com.github.alradas;

import org.bukkit.entity.ArmorStand;

public class ArmorStandObject {
	private ArmorStand armorStand = null;
	private String itemID = null;
	private int customModelData = 0;
	private String texture = null;
	
	public ArmorStandObject(ArmorStand varArmorStand, String varItemID, int varCustomModelData, String varTexture) {
		armorStand = varArmorStand;
		itemID = varItemID;
		customModelData = varCustomModelData;
		texture = varTexture;
	}
	
	public void setArmorStand(ArmorStand varArmorStand) {
		armorStand = varArmorStand;
	}
	
	public ArmorStand getArmorStand() {
		return armorStand;
	}
	public String getID() {
		return itemID;
	}
	public int getCustomModelData() {
		return customModelData;
	}
	public String getTexture() {
		return texture;
	}
}
