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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.rangun.luegenpresse.spew.DefnStringProvider;

public final class LuegenpressePlugin extends JavaPlugin {

	public final NamespacedKey BOOK_OF_LIES_KEY = new NamespacedKey(this, "book_of_lies");

	private final FileConfiguration config = getConfig();

	private File headline;

	private final DefnStringProvider online_dsp = new DefnStringProvider() {

		@Override
		public List<Byte> getString(int rnd) {

			final List<? extends Player> op = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
			final List<Byte> l;

			if (!op.isEmpty()) {
				l = Lists.newArrayList(ArrayUtils.toObject(
						op.get(Math.min(op.size() - 1, rnd % op.size())).getName().getBytes(StandardCharsets.UTF_8)));
			} else {
				l = Lists.newArrayList(ArrayUtils.toObject("FakeOnlinePlayer".getBytes(StandardCharsets.UTF_8)));
			}

			l.add((byte) '\0');

			return l;
		}
	};

	@Override
	public void onEnable() {

		createHeadline();

		config.addDefault("book_of_lies_title", "Book of Lies");
		config.addDefault("fake_newspaper_title", "National Enquirer");
		config.addDefault("fake_newspaper_author", "Baron Munchausen");
		config.addDefault("lie_broadcast_ticks", 54000L);
		config.options().copyDefaults(true);
		saveConfig();

		ShapedRecipe recipe = new ShapedRecipe(BOOK_OF_LIES_KEY, createBookOfLies(1));

		recipe.shape("DDD", "DBD", "DDD");
		recipe.setIngredient('D', Material.DIAMOND);
		recipe.setIngredient('B', Material.BOOK);

		Bukkit.addRecipe(recipe);

		getServer().getPluginManager().registerEvents(new JoinListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

		(new TellLieTask(this, null)).runTaskTimerAsynchronously(this, 600L, config.getLong("lie_broadcast_ticks"));

		final CommandTellLie tc = new CommandTellLie(this);
		final CommandLuegenpresse lc = new CommandLuegenpresse(this);

		getCommand("telllie").setExecutor(tc);
		getCommand("telllie").setTabCompleter(tc);

		getCommand("luegenpresse").setExecutor(lc);
		getCommand("luegenpresse").setTabCompleter(lc);
		
		final int pluginId = 15247;
		new Metrics(this, pluginId);
	}

	public ItemStack createBookOfLies(int amount) {

		final ItemStack book = new ItemStack(Material.WRITTEN_BOOK, amount);
		final BookMeta meta = (BookMeta) book.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN + config.getString("book_of_lies_title"));
		meta.setTitle(meta.getDisplayName());
		meta.setGeneration(Generation.TATTERED);
		meta.setAuthor(config.getString("fake_newspaper_author"));
		meta.addPage("tbw");
		// meta.setLore(null);

		meta.getPersistentDataContainer().set(BOOK_OF_LIES_KEY, PersistentDataType.BYTE, Byte.valueOf((byte) 0));
		book.setItemMeta(meta);

		return book;
	}

	public File getHeadline() {
		return headline;
	}

	public DefnStringProvider getOnlineDefnStringProvider() {
		return online_dsp;
	}

	private void createHeadline() {

		headline = new File(getDataFolder(), "headline");

		if (!headline.exists()) {

			headline.getParentFile().mkdirs();
			saveResource("headline", false);

			try {

				final BufferedReader in = new BufferedReader(new InputStreamReader(
						this.getClass().getResourceAsStream("/headline"), StandardCharsets.UTF_8));

				final FileWriter out = new FileWriter(headline);

				String line;

				while ((line = in.readLine()) != null) {
					out.write(line);
					out.write('\n');
				}

				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
