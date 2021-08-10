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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import de.rangun.luegenpresse.spew.Spew;
import de.rangun.luegenpresse.spew.SpewException;

public final class LuegenpresseCommand extends TellLie implements CommandExecutor, TabCompleter {

	private final static List<String> sub = new ArrayList<String>(2) {
		private static final long serialVersionUID = -2710518232153162079L;
		{
			add("help");
			add("reload");
		}
	};

	public LuegenpresseCommand(final LuegenpressePlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 1) {

			if (sub.get(0).equals(args[0])) {
				sender.sendMessage("tbw");
				return true;
			}

			if (sub.get(1).equals(args[0])) {

				try {

					final Spew spew = Spew.getInstance(plugin.getHeadline(), null);

					spew.reload();

					final String lie = getLie();

					sender.sendMessage("Headlines reloaded successfully.\n" + lie);

				} catch (SpewException e) {

					sender.sendMessage(ChatColor.RED + "Reload failed." + ChatColor.RESET
							+ " headline file contains errors: " + e.getMessage());

				} catch (IOException e) {

					sender.sendMessage(ChatColor.RED + "Reload failed." + ChatColor.RESET
							+ " headline file couldn't get loaded: " + e.getMessage());

				}

				return true;
			}
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		if (args.length != 0) {

			final List<String> proposals = Lists.newArrayList();

			for (String s : sub) {

				if (StringUtil.startsWithIgnoreCase(s, args[0])) {
					proposals.add(s);
				}
			}

			return proposals;
		}

		return sub;
	}

	@Override
	public void run() {
		// unused
	}
}
