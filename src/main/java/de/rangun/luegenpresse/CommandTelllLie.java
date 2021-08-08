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
import org.bukkit.entity.Player;

public final class CommandTelllLie extends TellLie implements CommandExecutor {

	private final FileConfiguration config;

	public CommandTelllLie(LuegenpressePlugin luegenpressePlugin) {
		super(luegenpressePlugin);
		this.config = luegenpressePlugin.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		try {
			Bukkit.getServer().broadcastMessage(getLie());
		} catch (Exception e) {

			Bukkit.getLogger().severe(e.getMessage());

			if (sender instanceof Player) {
				((Player) sender).sendMessage(ChatColor.GREEN + config.getString("fake_newspaper_title") + ": "
						+ ChatColor.RED + e.getMessage());
			}
		}

		return true;
	}

	@Override
	public void run() {
		// unused
	}
}
