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
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import de.rangun.luegenpresse.spew.Spew;
import de.rangun.luegenpresse.spew.SpewException;

public final class CommandLuegenpresse extends TellLie implements CommandExecutor, TabCompleter {

	private final static List<String> sub = new ArrayList<String>(2) {
		private static final long serialVersionUID = -2710518232153162079L;
		{
			add("help");
			add("reload");
			add("give");
		}
	};

	public CommandLuegenpresse(final LuegenpressePlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length > 0) {

			if (sub.get(0).equals(args[0])) {
				sender.sendMessage("tbw");
				return true;
			}

			if (sub.get(1).equals(args[0])) {

				try {

					final Spew spew = Spew.getInstance(plugin.getHeadline(), plugin.getOfflineDefnStringProvider(),
							plugin.getOnlineDefnStringProvider(), null);

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

			if (sub.get(2).equals(args[0])) {

				if (args.length > 1) {

					Player p = Bukkit.getPlayer(args[1]);

					if (p != null) {

						int amount = 1;

						if (args.length > 2) {
							try {
								amount = Integer.parseInt(args[2]);
							} catch (NumberFormatException e) {
							}
						}

						for (Entry<Integer, ItemStack> loi : p.getInventory().addItem(plugin.createBookOfLies(amount))
								.entrySet()) {
							p.getWorld().dropItem(p.getLocation().add(p.getLocation().getDirection()), loi.getValue());
						}

						p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 2.0f);
						p.sendMessage("You've just got " + amount + " " + ChatColor.DARK_GREEN + ChatColor.ITALIC
								+ plugin.getConfig().getString("book_of_lies_title") + ChatColor.RESET + " from "
								+ ChatColor.AQUA + sender.getName() + ChatColor.RESET + ".");

						sender.sendMessage("Gave " + amount + " " + ChatColor.DARK_GREEN + ChatColor.ITALIC
								+ plugin.getConfig().getString("book_of_lies_title") + ChatColor.RESET + " to "
								+ ChatColor.AQUA + p.getName() + ChatColor.RESET + ".");

					} else {
						sender.sendMessage(
								ChatColor.RED + "No player " + ChatColor.AQUA + args[1] + ChatColor.RED + " found.");
					}

				} else {
					sender.sendMessage("Please give me a plyer's name to give " + ChatColor.ITALIC
							+ plugin.getConfig().getString("book_of_lies_title") + ChatColor.RESET + " to.");
				}

				return true;
			}
		}

		return false;

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		final List<String> proposals = Lists.newArrayList();

		if (args.length == 1) {

			for (String s : sub) {

				if (StringUtil.startsWithIgnoreCase(s, args[0])) {
					proposals.add(s);
				}
			}

			return proposals;

		} else if (args.length == 2 && sub.get(2).equals(args[0])) {

			for (Player p : Bukkit.getOnlinePlayers()) {

				if (StringUtil.startsWithIgnoreCase(p.getName(), args[1])) {
					proposals.add(p.getName());
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
