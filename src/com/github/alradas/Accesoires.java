package com.github.alradas;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Accesoires extends JavaPlugin implements Listener {
	public Map<UUID, List<ArmorStandObject>> playersHeadwear = null;
	public Map<UUID, List<ArmorStandObject>> playersBodywear = null;
	File dataFolder = getDataFolder();
	File accListHeadwear = null;
	File accListBodywear = null;
	private String inventoryTitleHeadwear;
	private String inventoryTitleBodywear;
	
	@Override
	public void onEnable() {
		loadData();
		clearArmorStands();
		registerListener();
		fillData();
		Bukkit.getScheduler().runTaskTimer(this,() -> reloadArmorStands(),0L,1L);
		getLogger().info("Set task to reload armor stands!");
		getLogger().info("ACCESSOIRES enabled!");
	}
	private void loadData() {
		dataFolder = getDataFolder();
		if (!dataFolder.exists()) { dataFolder.mkdir(); }
		checkAndCreateAccList(dataFolder, "acclistHeadwear.json", true);
		checkAndCreateAccList(dataFolder, "acclistBodywear.json", false);
		playersHeadwear = readAccList(true);
		if (playersHeadwear == null) playersHeadwear = new HashMap<UUID, List<ArmorStandObject>>();
		playersBodywear = readAccList(false);
		if (playersBodywear == null) playersBodywear = new HashMap<UUID, List<ArmorStandObject>>();
		getLogger().info("Loaded Data!");
	}
	private void checkAndCreateAccList(File varDataFolder, String varFileName, Boolean varHeadwear) {
		File accList = new File(varDataFolder, varFileName);
		if (varHeadwear) { accListHeadwear = accList; } 
		else {			   accListBodywear = accList; }
		if (!accList.exists()) {
			try {
				accList.createNewFile();
			} catch (IOException e) {
				getLogger().severe("Error while creating the file \"" + varFileName + "\": " + e.toString());
			}
		}
	}
	private void clearArmorStands() {
		List<World> worlds = Bukkit.getServer().getWorlds();
		for (World world : worlds) {
			List<Entity> entities = world.getEntities();
			for (Entity entity : entities) {
				if(entity.getType().equals(EntityType.ARMOR_STAND) && entity.getName().equals("ACCESSOIRESTAND")) {
					entity.remove();
				}
			}
		}
		getLogger().info("Cleared existing armor stands!");
	}
	private void registerListener() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Listener registered!");
	}
	private void fillData() {
		inventoryTitleHeadwear = "Accessoires - Kopf";
		inventoryTitleBodywear = "Accessoires - Körper";
	}
	
	@Override
	public void onDisable() {
		write();
		clearArmorStands();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getLabel().equals("accessoire") && sender instanceof Player) {
			String subCommand = "none";
			if(args.length > 0) {
				subCommand = args[0];
			}
			
			if (subCommand.toLowerCase() == "reload" && sender.isOp()) {
				reload((Player)sender, args);
			} else if (subCommand.toLowerCase() == "save" && sender.isOp()) {
				write();
			} else {
				accessoire((Player)sender, args);
			}
		}
		return true;
	}
	
	private boolean reload(Player player, String[] args) {
		String subCommandSpec = "none";
		if (args.length > 1) {
			subCommandSpec = args[1];
		}
		if (!subCommandSpec.equalsIgnoreCase("bodywear")) {
			playersHeadwear = readAccList(true);
		}
		if (!subCommandSpec.equalsIgnoreCase("headwear")) {
			playersBodywear = readAccList(false);
		}
		return true;
	}
	private boolean accessoire(Player player, String[] args) {
		String subCommandSpec = "none";
		if (args.length > 0) {
			subCommandSpec = args[0];
		}
		if(subCommandSpec.equalsIgnoreCase("headwear")
				 || subCommandSpec.equalsIgnoreCase("head")
				 || subCommandSpec.equalsIgnoreCase("h")) {
			openAccInventory(player, true);
		} else if(subCommandSpec.equalsIgnoreCase("bodywear")
			 || subCommandSpec.equalsIgnoreCase("body")
			 || subCommandSpec.equalsIgnoreCase("b")) {
			//TODO: If player body rotation can be gotten - implement body
			openAccInventory(player, true);
		} else {
			//TODO: Change to error message
			openAccInventory(player, true);
		}
        return true;
	}
	private ItemStack createItemStack(String varId, int varCustomModelData, String varTexture, Boolean varHeadwear) {
		Material material = Material.getMaterial(varId);
		ItemStack retItem = new ItemStack(material, 1);
		ItemMeta itemMeta = null;
		if (retItem.hasItemMeta()) {
			itemMeta = retItem.getItemMeta();
		} else {
			itemMeta = Bukkit.getItemFactory().getItemMeta(retItem.getType());
		}
		if (varCustomModelData != 0)
			itemMeta.setCustomModelData(varCustomModelData);
		if (material.equals(Material.PLAYER_HEAD)) {
			//TODO: No Skulls allowed (yet). Sowwy.
		}
		itemMeta.setDisplayName(ChatColor.WHITE + "Accessoireitem " + ((varHeadwear) ? "Kopf" : "Körper"));
		retItem.setItemMeta(itemMeta);
		return retItem;
	}
	private ArmorStand createArmorStand(ItemStack varItem, Player player) {
		Location playerLoc = player.getLocation().add(0, 0, 0);
        ArmorStand accessoireStand = playerLoc.getWorld().spawn(playerLoc, ArmorStand.class);
        accessoireStand.setGravity(false);
        accessoireStand.setCanPickupItems(false);
        accessoireStand.setCustomName("ACCESSOIRESTAND");
        accessoireStand.setCustomNameVisible(false);
        accessoireStand.setVisible(false);
        accessoireStand.setInvulnerable(true);
        accessoireStand.setHelmet(varItem);
        accessoireStand.setMarker(true);
		return accessoireStand;
	}
	private ArmorStandObject createArmorStandObject(ArmorStand accStand) {
		ItemStack headItem = accStand.getHelmet();
		Material headType = headItem.getType();
		int customModelData = 0;
		String texture = "";
		if (headItem.hasItemMeta()) {
			ItemMeta headMeta = headItem.getItemMeta();
			if (headMeta.hasCustomModelData()) {
				customModelData = headItem.getItemMeta().getCustomModelData();
			}
			if (headType == Material.PLAYER_HEAD) {
				texture = getTextureFromHead(headMeta);
			}
		}
		
		ArmorStandObject armorStandObject = new ArmorStandObject(accStand, headType.toString(), customModelData, texture);
		return armorStandObject;
	}
	private String getTextureFromHead(ItemMeta varMeta) {
		String texture = "";
		try {
			SkullMeta skullMeta = (SkullMeta)varMeta;
			String uuid = skullMeta.getOwningPlayer().getUniqueId().toString();
			URL url;
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            texture = textureProperty.get("value").getAsString();
		} catch (IOException e) {
			getLogger().warning("Error while loading texture of skull: " + e.toString());
		}
		return texture;
	}
	private void openAccInventory(Player varPlayer, Boolean varHeadwear) {
		Inventory inv = Bukkit.createInventory(null, 27, ((varHeadwear) ? inventoryTitleHeadwear : inventoryTitleBodywear));
		UUID playerUUID = varPlayer.getUniqueId();
		if (((varHeadwear) ? playersHeadwear : playersBodywear).containsKey(playerUUID)) {
			List<ArmorStandObject> armorStandObjects = ((varHeadwear) ? playersHeadwear : playersBodywear).get(playerUUID);
			int currentChestSlot = 0;
			for (ArmorStandObject armorStandObject : armorStandObjects) {
				ItemStack item = createItemStack(armorStandObject.getID(), armorStandObject.getCustomModelData(), armorStandObject.getTexture(), varHeadwear);
				inv.setItem(currentChestSlot, item);
				currentChestSlot++;
			}
		}
		varPlayer.openInventory(inv);
	}
	
	private boolean write() {
		Boolean headwear = writeHelper(accListHeadwear, playersHeadwear);
		Boolean bodywear = writeHelper(accListBodywear, playersBodywear);
		if (!headwear || !bodywear) {
			return false;
		}
		return true;
	}
	private boolean writeHelper(File varAccList, Map<UUID, List<ArmorStandObject>> varPlayers) {
		try {
			FileWriter fw = new FileWriter(varAccList, false);
			String accessoirelist = stringify(varPlayers);
			fw.write(accessoirelist);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			getLogger().severe("Error while writing accessoire list: " + e.toString());
			return false;
		}
		return true;
	}
	private String stringify(Map<UUID, List<ArmorStandObject>> playermap) {
		JsonObject serialization = new JsonObject();
		Iterator<Map.Entry<UUID,List<ArmorStandObject>>> playerIt = playermap.entrySet().iterator();
        while (playerIt.hasNext()) {
            Map.Entry<UUID,List<ArmorStandObject>> pair = playerIt.next();
            
            UUID playerUUID = pair.getKey();
            JsonObject playerObject = new JsonObject();
            playerObject = getPlayerObject(pair);
            
            serialization.add(playerUUID.toString(), playerObject);
        }
	    return serialization.toString();
	}
	private JsonObject getPlayerObject(Map.Entry<UUID,List<ArmorStandObject>> pair) {
        JsonObject playerObject = new JsonObject();
		List<ArmorStandObject> list = pair.getValue();
        for (int arrayInt = 0; arrayInt < list.size(); arrayInt++) {
        	ArmorStandObject accStandObject = list.get(arrayInt);
        	if (accStandObject != null) {
        		JsonObject armorStandObject = new JsonObject();
        		armorStandObject.addProperty("id", accStandObject.getID());
        		armorStandObject.addProperty("customModelData", accStandObject.getCustomModelData());
        		armorStandObject.addProperty("headTexture", accStandObject.getTexture());
        		
            	playerObject.add(String.valueOf(arrayInt), armorStandObject);
        	}
        }
        return playerObject;
	}
	
	private Map<UUID, List<ArmorStandObject>> readAccList(Boolean varHeadwear) {
		Map<UUID, List<ArmorStandObject>> retList = null;
		try {
			List<String> content = Files.readAllLines(((varHeadwear) ? accListHeadwear : accListBodywear).toPath());
			String delim = System.lineSeparator();
			String contentString = String.join(delim, content);
			if (contentString == null) {
				return null;
			} else if (contentString.equals("")) {
				return null;
			}
			retList = parse(contentString);
		} catch (IOException e) {
			getLogger().severe("Error while loading accessoire list: " + e.toString());
			return null;
		}
		return retList;
	}
	private Map<UUID, List<ArmorStandObject>> parse(String jsonString) {
		Map<UUID, List<ArmorStandObject>> retVal = new HashMap<UUID, List<ArmorStandObject>>();
		JsonObject convertedObject = new Gson().fromJson(jsonString, JsonObject.class);
		for(Map.Entry<String, JsonElement> playerElement : convertedObject.entrySet()) {
			UUID playerUUID = UUID.fromString(playerElement.getKey());
			JsonObject armorStandList = playerElement.getValue().getAsJsonObject();
			ArrayList<ArmorStandObject> armorStands = new ArrayList<ArmorStandObject>();
			for(Map.Entry<String, JsonElement> armorStandElement : armorStandList.entrySet()) {
				armorStands.add(createArmorStandObject(armorStandElement.getValue()));
			}
			retVal.put(playerUUID, armorStands);
		}
		
		return retVal;
	}
	private ArmorStandObject createArmorStandObject(JsonElement varArmorStandJsonElement) {
		JsonObject armorStandObject = varArmorStandJsonElement.getAsJsonObject();
		JsonElement objId = armorStandObject.get("id");
		String id = "";
		if (!objId.equals(null)) id = objId.getAsString();
		
		JsonElement objCustomModelData = armorStandObject.get("customModelData");
		int customModelData = 0;
		if (!objId.equals(null)) customModelData = objCustomModelData.getAsInt();
		
		JsonElement objHeadTexture = armorStandObject.get("headTexture");
		String headTexture = "";
		if (!objId.equals(null)) headTexture = objHeadTexture.getAsString();
		
		return new ArmorStandObject(null, id, customModelData, headTexture);
	}

	private void reloadArmorStands() {
		for(Player player : Bukkit.getOnlinePlayers()){
		    UUID playerUUID = player.getUniqueId();
		    List<ArmorStandObject> listHeadwear = playersHeadwear.get(playerUUID);
		    if (listHeadwear != null) {
		    	onlinePlayerHandlingHeadwear(listHeadwear, player);
		    }
		    List<ArmorStandObject> listBodywear = playersBodywear.get(playerUUID);
		    if (listBodywear != null) {
		    	onlinePlayerHandlingBodywear(listBodywear, player);
		    }
		}
	}
	private void onlinePlayerHandlingHeadwear(List<ArmorStandObject> varArmorStandList, Player varPlayer) {
        Location playerLoc = varPlayer.getLocation().clone();
    	Float playerPitch = varPlayer.getEyeLocation().getPitch();
    	double yint = playerPitch/Math.PI/17.5;
    	EulerAngle playerHeadPose = new EulerAngle(yint, 0, 0);
        
        for (int arrayInt = 0; arrayInt < varArmorStandList.size(); arrayInt++) {
        	ArmorStandObject accStandObject = varArmorStandList.get(arrayInt);
        	ArmorStand accStand = accStandObject.getArmorStand();
        	if (accStand == null) {
        		ItemStack headItem = createItemStack(accStandObject.getID(), accStandObject.getCustomModelData(), accStandObject.getTexture(), true);
        		accStand = createArmorStand(headItem, varPlayer);
        		accStandObject.setArmorStand(accStand);
        	}
        	
        	if (accStand != null) {
        		accStand.teleport(playerLoc);
        		accStand.setHeadPose(playerHeadPose);
        	}
        }
	}
	private void onlinePlayerHandlingBodywear(List<ArmorStandObject> varArmorStandList, Player varPlayer) {
		//TODO: Einrichten der Bodywear-Handling-Funktion
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<ArmorStandObject> listHeadwear = playersHeadwear.get(player.getUniqueId());
        offlinePlayerHandling(listHeadwear);
        List<ArmorStandObject> listBodywear = playersBodywear.get(player.getUniqueId());
        offlinePlayerHandling(listBodywear);
	}
	private void offlinePlayerHandling(List<ArmorStandObject> varArmorStandList) {
		if (varArmorStandList != null) {
			removeAllArmorStands(varArmorStandList);
		}
	}
	private void removeAllArmorStands(List<ArmorStandObject> varArmorStandList) {
		for (int arrayInt = 0; arrayInt < varArmorStandList.size(); arrayInt++) {
        	ArmorStandObject accStandObject = varArmorStandList.get(arrayInt);
			ArmorStand accStand = accStandObject.getArmorStand();
			if (accStand != null) {
				accStand.remove();
				accStandObject.setArmorStand(null);
			}
		}
	}
	
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent event) {
		String viewTitle = event.getView().getTitle();
		if (viewTitle.equals(inventoryTitleHeadwear) || viewTitle.equals(inventoryTitleBodywear)) {
			Boolean varHeadwear = viewTitle.equals(inventoryTitleHeadwear);
			Player player = (Player)event.getPlayer();
			List<ArmorStandObject> armorStands = ((varHeadwear) ? playersHeadwear : playersBodywear).get(player.getUniqueId());
			((varHeadwear) ? playersHeadwear : playersBodywear).remove(player.getUniqueId());
			if (armorStands != null) {
				removeAllArmorStands(armorStands);
			}
			List<ArmorStandObject> newList = createNewList(event.getInventory().getContents(), player);
			((varHeadwear) ? playersHeadwear : playersBodywear).put(player.getUniqueId(), newList);
			write();
		}
	}
	private List<ArmorStandObject> createNewList(ItemStack[] varItemStacks, Player player) {
		List<ArmorStandObject> retList = new ArrayList<ArmorStandObject>();
		for(ItemStack itemStack : varItemStacks) {
			if (itemStack == null) {}
			else if (itemStack.getType().equals(Material.AIR)) {}
			else {
				ArmorStand accStand = createArmorStand(itemStack, player);
				ArmorStandObject accStandObject = createArmorStandObject(accStand);
				retList.add(accStandObject);
			}
		}
		return retList;
	}
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		String viewTitle = event.getView().getTitle();
		if (viewTitle.equals(inventoryTitleHeadwear) || viewTitle.equals(inventoryTitleBodywear)) {
			Boolean isHeadwear = viewTitle.equals(inventoryTitleHeadwear);
			ClickType clickType = event.getClick();
			InventoryType invType = getInventoryType(event);
			if (clickType.equals(ClickType.CONTROL_DROP)) {
				handleInvClickDrop(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.DOUBLE_CLICK)) {
				handleInvClickClick(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.DROP)) {
				handleInvClickDrop(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.LEFT)) {
				handleInvClickClick(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.RIGHT)) {
				handleInvClickClick(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.SHIFT_LEFT)) {
				handleInvClickShiftClick(event, invType, isHeadwear);
			} else if (clickType.equals(ClickType.SHIFT_RIGHT)) {
				handleInvClickShiftClick(event, invType, isHeadwear);
			}
		}
	}
	private void handleInvClickDrop(InventoryClickEvent varEvent, InventoryType varInvType, Boolean varHeadwear) {
        if (varInvType.equals(InventoryType.ACCESSOIRE)) {
        	varEvent.setCancelled(true);
        }
	}
	private void handleInvClickClick(InventoryClickEvent varEvent, InventoryType varInvType, Boolean varHeadwear) {
        if (varInvType.equals(InventoryType.ACCESSOIRE)) {
        	Player player = (Player)varEvent.getWhoClicked();
        	ItemStack itemOnCursor = player.getItemOnCursor();
        	ItemStack emptyItem = new ItemStack(Material.AIR, 1);
        	if (itemOnCursor == null) {
                varEvent.setCurrentItem(emptyItem);
        	} else if (itemOnCursor.getType().equals(Material.AIR)) {
                varEvent.setCurrentItem(emptyItem);
        	} else {
                ItemStack normalizedItem = createNormalizedItem(itemOnCursor, varHeadwear);
                varEvent.setCurrentItem(normalizedItem);
        	}
        	varEvent.setCancelled(true);
        }
	}
	private void handleInvClickShiftClick(InventoryClickEvent varEvent, InventoryType varInvType, Boolean varHeadwear) {
		if (varInvType.equals(InventoryType.ACCESSOIRE)) {
			ItemStack emptyItem = new ItemStack(Material.AIR, 1);
			varEvent.setCurrentItem(emptyItem);
		} else if (varInvType.equals(InventoryType.PLAYER)) {
			ItemStack clickedItem = varEvent.getCurrentItem();
			if (clickedItem == null) { }
			else if (clickedItem.getType().equals(Material.AIR)) { }
			else {
				ItemStack normalizedItem = createNormalizedItem(clickedItem, varHeadwear);
				Inventory topInventory = varEvent.getView().getTopInventory();
				int firstEmpty = topInventory.firstEmpty();
				if (firstEmpty < topInventory.getSize() && firstEmpty != -1) {
					topInventory.setItem(firstEmpty, normalizedItem);
				}
			}
		}
		varEvent.setCancelled(true);
	}
	
	private InventoryType getInventoryType(InventoryClickEvent varEvent) {
		Inventory inventory = varEvent.getClickedInventory();
		Player player = (Player)varEvent.getWhoClicked();
		return getInventoryType(player, inventory);
	}
	private InventoryType getInventoryType(Player varPlayer, Inventory varInventory) {
		if (varInventory == null) {
			return InventoryType.NONE;
		} else if (varInventory.equals(varPlayer.getInventory())) {
			return InventoryType.PLAYER;
		} else {
			return InventoryType.ACCESSOIRE;
		}
	}
	@EventHandler
	private void onInventoryDrag(InventoryDragEvent event) {
		String viewTitle = event.getView().getTitle();
		if (viewTitle.equals(inventoryTitleHeadwear) || viewTitle.equals(inventoryTitleBodywear)) {
			Boolean isHeadwear = viewTitle.equals(inventoryTitleHeadwear);
			Inventory inventory = event.getInventory();
			ItemStack item = getFirstNewItem(event.getNewItems());
			if (item != null) {
				ItemStack newItem = createNormalizedItem(item, isHeadwear);
				Set<Integer> slotsToFill = event.getRawSlots();
				for(int slotNumber : slotsToFill) {
					if (slotNumber < event.getView().getTopInventory().getSize()) {
						inventory.setItem(slotNumber, newItem);
					}
				}
			}
			event.setCancelled(true);
		}
	}
	private ItemStack getFirstNewItem(Map<Integer, ItemStack> varItemList) {
		Iterator<Map.Entry<Integer, ItemStack>> iterator = varItemList.entrySet().iterator();
		if (iterator.hasNext()) {
			return iterator.next().getValue();
		} else return null;
	}
	private ItemStack createNormalizedItem(ItemStack varItem, Boolean varHeadwear) {
		String id = varItem.getType().toString();
		int customModelData = 0;
		String texture = "";
		if (varItem.hasItemMeta()) {
			if (varItem.getItemMeta().hasCustomModelData())
				customModelData = varItem.getItemMeta().getCustomModelData();
			if (varItem.getType().equals(Material.PLAYER_HEAD)) {
				texture = getTextureFromHead(varItem.getItemMeta());
			}
		}
		return createItemStack(id, customModelData, texture, varHeadwear);
	}

	@EventHandler
	private void onBlockPlacement(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta.hasCustomModelData()) {
				Player player = event.getPlayer();
				if (player.isOp() && player.getGameMode().equals(GameMode.CREATIVE)) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Die Sperre, Items mit besonderen Texturen zu platzieren, wird umgangen!"));
				} else {
					event.setCancelled(true);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&lDieser Gegenstand kann nicht platziert werden!"));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Er hat eine besondere Textur, die dann verloren gehen würde."));
				}
			}
		}
	}
	
	enum InventoryType {
		PLAYER,
		ACCESSOIRE,
		NONE
	}
}