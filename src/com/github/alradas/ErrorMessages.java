package com.github.alradas;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ErrorMessages {
	//Supportfunctions
	private static void SendDivider(Player player) {
		player.sendMessage(format("&7####################################"));
	}
	private static void SendDefaultFormat(Player player) { 
		SendDivider(player);
		player.sendMessage(format("&8Korrektes Format:"));
		player.sendMessage(format("&7/prospect (&6Gesteinskundewert&7) (&6Level des Erzes/Gesteins&7) [&6Menge der Stapel&7]"));
		SendDivider(player);
		player.sendMessage(format("&8Beispiel:"));
		player.sendMessage(format("&7Der Gesteinskundler &6Spat Tharim möchte mit Gesteinskunde &675&7 insgesamt &65 Stapel&7 von seinem &6Eisenerz (Level 4)&7 prospektieren. Er gibt ein:"));
		player.sendMessage(format("&6/prospect 75 4 5 Spat Tharim"));
	}
	private static String format (String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	//Errormessages
	public static void TooFewArguments(Player player) {
		SendDivider(player);
		player.sendMessage(format("&6Du hast zu wenige Werte mitgegeben!"));
		SendDefaultFormat(player);
	}

	public static void TooManyArguments(Player player) {
		SendDivider(player);
		player.sendMessage(format("&6Du hast zu viele Werte mitgegeben!"));
		SendDefaultFormat(player);
	}

	public static void WrongInput(Player player, int argNumber, String argument) {
		SendDivider(player);
		player.sendMessage(format("&6Deine " + (argNumber == 0? "erste Variable" : (argNumber == 1? "zweite Variable" : (argNumber == 2? "dritte Variable" : "Eingabe"))) + " war wohl falsch!"));
		player.sendMessage(format("&6Ich habe \"&e" + argument + "&6\" leider nicht verstanden!"));
		if (argNumber == 2) {
			player.sendMessage(format("&6Ich habe stattdessen einfach &e1 als Menge eingesetzt!"));
			player.sendMessage(format("&6Trotzdem hier noch einmal das Standardformat, bevor es los geht:"));
		}
		SendDefaultFormat(player);
	}

	public static void UnsupportedCase(Player player) {
		player.sendMessage(format("&6Deine Eingabe war wohl falsch!"));
		SendDefaultFormat(player);
	}
	
	public static void TooHigh(Player player, String type, int value, int maximum) {
		OutOfBounds(player, type, value, maximum, true);
	}
	public static void TooLow(Player player, String type, int value, int minimum) {
		OutOfBounds(player, type, value, minimum, false);
	}
	private static void OutOfBounds(Player player, String type, int value, int boundary, Boolean tooHigh) {
		SendDivider(player);
		player.sendMessage(format("&6" + (type.equalsIgnoreCase("ability")? "Dein Fähigkeitswert" : (type.equalsIgnoreCase("level")? "Die Stufe" : (type.equalsIgnoreCase("amount")? "Die Menge" : "Die Eingabe"))) 
				+ " ist zu " + (tooHigh? "hoch" : "niedrig") + "!"));
		player.sendMessage(format("&6" + (type.equalsIgnoreCase("amount")? "Dein " : "Das ") + (tooHigh? "Maximum" : "Minimum") + " ist &e" + boundary + "&6, du hast aber \"&e" + value + "&6\" eingegeben!"));
		SendDefaultFormat(player);
	}
}
