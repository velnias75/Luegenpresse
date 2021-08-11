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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

public final class CommandTellLie extends TellLie implements CommandExecutor, TabCompleter {

	private final FileConfiguration config;

	public CommandTellLie(LuegenpressePlugin luegenpressePlugin) {
		super(luegenpressePlugin);
		this.config = luegenpressePlugin.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		try {

			if (args.length > 0) {

				final Player p = Bukkit.getPlayer(args[0]);

				if (p != null) {

					final String lie = getLie();

					p.sendMessage(lie);
					sender.sendMessage("Sent lie to " + ChatColor.AQUA + p.getName() + ChatColor.RESET + ":\n" + lie);

				} else {
					sender.sendMessage(
							ChatColor.RED + "Player " + ChatColor.AQUA + args[0] + ChatColor.RED + " not found.");
				}

			} else {
				Bukkit.getServer().broadcastMessage(getLie());
			}

		} catch (Exception e) {

			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);

			e.printStackTrace(pw);

			Bukkit.getLogger().severe(sw.toString());

			if (sender instanceof Player) {
				((Player) sender).sendMessage(ChatColor.GREEN + config.getString("fake_newspaper_title") + ": "
						+ ChatColor.RED + e.getMessage());
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		List<String> proposals = Lists.newArrayList();

		if (args.length == 1) {

			for (Player p : Bukkit.getOnlinePlayers()) {

				if (StringUtil.startsWithIgnoreCase(p.getName(), args[0])) {
					proposals.add(p.getName());
				}
			}
		}

		return proposals;
	}

	@Override
	public void run() {
		// unused
	}
}
