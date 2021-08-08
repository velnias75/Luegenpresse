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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import de.rangun.luegenpresse.spew.Spew;

public final class LiarLecternListener implements Listener {

	private final FileConfiguration config;
	private final NamespacedKey nk;
	private final LuegenpressePlugin plugin;

	public LiarLecternListener(final LuegenpressePlugin plugin) {
		this.nk = new NamespacedKey(plugin, "LyingLectern");
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}

	@EventHandler
	public void onPlayerTakeLecternBook(final PlayerTakeLecternBookEvent event) {

		final PersistentDataContainer data = event.getLectern().getPersistentDataContainer();
		final ItemStack book = event.getBook();

		if (data.has(nk, PersistentDataType.BYTE)
				&& Byte.valueOf((byte) 1).equals(data.get(nk, PersistentDataType.BYTE))) {

			if (book != null && Material.WRITABLE_BOOK.equals(book.getType())) {

				final ItemMeta meta = book.getItemMeta();

				if (meta instanceof BookMeta) {

					final BookMeta bm = (BookMeta) meta;

					if (!bm.hasPages()) {

						bm.setTitle(config.getString("fake_newspaper_title"));
						bm.setDisplayName(bm.getTitle());
						bm.setAuthor(config.getString("fake_newspaper_author"));
						bm.setGeneration(Generation.TATTERED);

						try {

							int attempto1 = 0;
							int attempto2 = 0;

							final Spew spew = new Spew(plugin.getHeadline());
							final Set<String> lieheadlines = new HashSet<>(100);

							do {

								String lie;

								do {
									lie = spew.getHeadline();
									++attempto2;
								} while (lie.length() > 110 && attempto2 < 1024);

								lieheadlines.add(lie);
								++attempto1;

							} while (lieheadlines.size() < 100 && attempto1 < 1024);

							final Iterator<String> iter = lieheadlines.iterator();

							while (iter.hasNext()) {

								String liepage = iter.next();

								if (iter.hasNext()) {
									liepage += "\n\n-*-\n\n";
									liepage += iter.next();
								}

								bm.addPage(liepage);
							}

						} catch (Exception e) {
							Bukkit.getLogger().severe(e.getMessage());
							bm.addPage("I'm a too honest person, and wasn't able to tell lies to you.");
						}

						book.setType(Material.WRITTEN_BOOK);
						book.setItemMeta(meta);

						event.getLectern().getInventory().setItem(0, book);

						event.getPlayer().spawnParticle(Particle.SOUL, event.getPlayer().getLocation(), 10);
						event.getPlayer().sendMessage(
								"Lucky " + event.getPlayer().getDisplayName() + ", you got a lying newspaper!");

					} else {
						event.getPlayer().sendMessage("WTF? I'm only able to write lies to empty books!");
					}
				}
			}

		} else if (book != null && Material.WRITTEN_BOOK.equals(book.getType()) && book.getItemMeta()
				.getPersistentDataContainer().has(plugin.BOOK_OF_LIES_KEY, PersistentDataType.BYTE)) {

			if (Byte.valueOf((byte) 0).equals(book.getItemMeta().getPersistentDataContainer()
					.get(plugin.BOOK_OF_LIES_KEY, PersistentDataType.BYTE))) {

				event.getPlayer().spawnParticle(Particle.SOUL, event.getLectern().getLocation(), 20);
				data.set(nk, PersistentDataType.BYTE, Byte.valueOf((byte) 1));
				event.getLectern().update();

				final ItemMeta meta = (BookMeta) book.getItemMeta();

				if (meta instanceof BookMeta) {

					final BookMeta bm = (BookMeta) meta;

					bm.getPersistentDataContainer().set(plugin.BOOK_OF_LIES_KEY, PersistentDataType.BYTE,
							Byte.valueOf((byte) 1));
					bm.setDisplayName("used " + bm.getDisplayName());
					bm.setTitle(bm.getDisplayName());
					bm.addPage("Liar!");

					book.setType(Material.WRITTEN_BOOK);
					book.setItemMeta(bm);

					event.getLectern().getInventory().setItem(0, book);
				}
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "This Book of Lies has already been used.");
			}
		}
	}
}
