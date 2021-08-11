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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "Luegenpresse", version = "0.0-SNAPSHOT")
@Description(value = "A plugin to generate a newspaper of lies")
@Website(value = "https://github.com/velnias75/Luegenpresse")
@ApiVersion(Target.v1_16)
@Author(value = "Velnias75")
@Author(value = "Gregory Smith")
@Command(name = "telllie", desc = "Tell a random lie to one or all players", usage = "/telllie [player]", permission = "luegenpresse.tellie")
@Command(name = "luegenpresse", desc = "Administrative lie commands", usage = "/luegenpresse help|reload|give", permission = "luegenpresse.luegenpresse")
@Permission(name = "luegenpresse.lie_broadcast_join", desc = "Allows you to receive a lie broadcast on join", defaultValue = PermissionDefault.TRUE)
@Permission(name = "luegenpresse.lie_broadcast_receiver", desc = "Allows you to receive a lie broadcast", defaultValue = PermissionDefault.TRUE)
public final class LuegenpressePlugin extends JavaPlugin {

	public final NamespacedKey BOOK_OF_LIES_KEY = new NamespacedKey(this, "book_of_lies");

	private final FileConfiguration config = getConfig();

	private File headline;

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
