/*
 * Copyright 2021-2022 by Heiko Schäfer <heiko@rangun.de>
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

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.rangun.spiget.MessageRetriever;

public final class JoinListener implements Listener {

	private final LuegenpressePlugin plugin;
	private final MessageRetriever msgs;

	public JoinListener(final LuegenpressePlugin plugin, final MessageRetriever msgs) {
		this.plugin = plugin;
		this.msgs = msgs;
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {

		if (event.getPlayer().hasPermission("luegenpresse.lie_broadcast_join"))
			(new TellLieTask(plugin, event.getPlayer())).runTaskLater(plugin, 100L);

		if (event.getPlayer().isOp()) {

			for (String jm : msgs.getJoinMessages()) {
				event.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.ITALIC + "["
						+ plugin.getDescription().getName() + ": " + jm + "]");
			}
		}
	}
}
