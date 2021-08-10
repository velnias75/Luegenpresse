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
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import de.rangun.luegenpresse.spew.Spew;

public final class PlayerInteractListener implements Listener {

	private final NamespacedKey nk;
	private final FileConfiguration config;
	private final LuegenpressePlugin plugin;

	public PlayerInteractListener(final LuegenpressePlugin plugin) {
		this.nk = new NamespacedKey(plugin, "LyingLectern");
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {

		final Block b = event.getClickedBlock();

		if (b != null && Material.LECTERN.equals(b.getType())) {

			final Lectern lectern = (Lectern) b.getState();
			final PersistentDataContainer data = lectern.getPersistentDataContainer();
			final ItemStack book = lectern.getInventory().getItem(0);
			final ItemStack handbook = event.getPlayer().getInventory().getItemInMainHand();

			if (data.has(nk, PersistentDataType.BYTE)
					&& Byte.valueOf((byte) 1).equals(data.get(nk, PersistentDataType.BYTE))) {

				if (book == null && handbook != null) {

					if (Material.WRITABLE_BOOK.equals(handbook.getType())) {

						final ItemMeta meta = handbook.getItemMeta();

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

								handbook.setType(Material.WRITTEN_BOOK);
								handbook.setItemMeta(meta);

								lectern.getInventory().setItem(0, book);

								event.getPlayer().getWorld().spawnParticle(Particle.SOUL,
										event.getPlayer().getLocation(), 10);
								event.getPlayer().sendMessage("Lucky " + event.getPlayer().getDisplayName()
										+ ", you've just created a lying newspaper!");

							} else {
								event.getPlayer().sendMessage("WTF? I'm only able to write lies to empty books!");
							}
						}
					}
				}

			} else {

				if (book == null && handbook != null && Material.WRITTEN_BOOK.equals(handbook.getType())
						&& handbook.getItemMeta().getPersistentDataContainer().has(plugin.BOOK_OF_LIES_KEY,
								PersistentDataType.BYTE)) {

					if (Byte.valueOf((byte) 0).equals(handbook.getItemMeta().getPersistentDataContainer()
							.get(plugin.BOOK_OF_LIES_KEY, PersistentDataType.BYTE))) {

						event.getPlayer().getWorld().spawnParticle(Particle.SOUL, lectern.getLocation(), 20);
						data.set(nk, PersistentDataType.BYTE, Byte.valueOf((byte) 1));
						lectern.update();

						final ItemMeta meta = (BookMeta) handbook.getItemMeta();

						if (meta instanceof BookMeta) {

							final BookMeta bm = (BookMeta) meta;

							bm.getPersistentDataContainer().set(plugin.BOOK_OF_LIES_KEY, PersistentDataType.BYTE,
									Byte.valueOf((byte) 1));
							bm.setDisplayName("used " + bm.getDisplayName());
							bm.setTitle(bm.getDisplayName());
							bm.addPage("Liar!");

							handbook.setType(Material.WRITTEN_BOOK);
							handbook.setItemMeta(bm);

							lectern.getInventory().setItem(0, handbook);
						}

						event.getPlayer().getInventory().remove(event.getPlayer().getInventory().getItemInMainHand());

					} else {
						event.getPlayer().sendMessage(ChatColor.RED + "This Book of Lies has already been used.");
					}
				}
			}
		}
	}
}
