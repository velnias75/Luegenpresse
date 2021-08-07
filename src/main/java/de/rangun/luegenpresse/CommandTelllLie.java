/*
 * Copyright 2021 by Heiko Schäfer <heiko@rangun.de>
 *
 * This file is part of Luegenpresse.
 *
 * Luegenpresse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Luegenpresse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Luegenpresse.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rangun.luegenpresse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import de.rangun.luegenpresse.spew.Spew;

public final class CommandTelllLie implements CommandExecutor {

	private final LuegenpressePlugin plugin;
	private final FileConfiguration config;

	public CommandTelllLie(LuegenpressePlugin luegenpressePlugin) {
		this.plugin = luegenpressePlugin;
		this.config = luegenpressePlugin.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		try {

			String lie = (new Spew(plugin.getHeadline())).getHeadline();
			lie = lie.substring(0, lie.length() - 1);

			Bukkit.getServer().broadcastMessage(
					ChatColor.GREEN + config.getString("fake_newspaper_title") + ":\n" + ChatColor.DARK_PURPLE + lie);

		} catch (Exception e) {

			Bukkit.getLogger().severe(e.getMessage());
			Bukkit.getServer().broadcastMessage(
					ChatColor.GREEN + config.getString("fake_newspaper_title") + ": " + ChatColor.RED + e.getMessage());

		}

		return true;
	}
}
