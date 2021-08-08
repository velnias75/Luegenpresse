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
import org.bukkit.entity.Player;

public final class TellLieTask extends TellLie {

	private final Player player;

	public TellLieTask(final LuegenpressePlugin plugin, final Player player) {
		super(plugin);
		this.player = player;
	}

	@Override
	public void run() {

		try {

			if (player != null) {

				player.sendMessage(getLie());

			} else {

				final String lie = getLie();

				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage(lie);
				}
			}

		} catch (Exception e) {
			Bukkit.getLogger().severe(e.getMessage());
		}
	}
}
