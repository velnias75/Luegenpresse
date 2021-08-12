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

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import de.rangun.luegenpresse.spew.Spew;
import de.rangun.luegenpresse.spew.SpewException;

abstract class TellLie extends BukkitRunnable {

	protected final LuegenpressePlugin plugin;

	protected TellLie(final LuegenpressePlugin plugin) {
		this.plugin = plugin;
	}

	protected String getLie() throws IOException, SpewException {

		final Spew spew = Spew.getInstance(plugin.getHeadline(), plugin.getOfflineDefnStringProvider(),
				plugin.getOnlineDefnStringProvider(), null);
		String lie = spew.getHeadline();

		if (lie.endsWith("\n")) {
			lie = lie.substring(0, lie.length() - 1);
		}

		return ChatColor.GREEN + plugin.getConfig().getString("fake_newspaper_title") + ":\n" + ChatColor.LIGHT_PURPLE
				+ lie;
	}

}
