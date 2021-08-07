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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
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
@Commands(@Command(name = "telllie", desc = "Tell a random lie to all players", usage = "/telllie", permission = "luegenpresse.tellie"))
public final class LuegenpressePlugin extends JavaPlugin {

	public final NamespacedKey BOOK_OF_LIES_KEY = new NamespacedKey(this, "book_of_lies");

	private final FileConfiguration config = getConfig();

	@Override
	public void onEnable() {

		config.addDefault("book_of_lies_title", "Book of Lies");
		config.addDefault("fake_newspaper_title", "National Enquirer");
		config.addDefault("fake_newspaper_author", "Baron Munchausen");
		config.options().copyDefaults(true);
		saveConfig();

		final ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		final BookMeta meta = (BookMeta) book.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN + config.getString("book_of_lies_title"));
		meta.setTitle(meta.getDisplayName());
		meta.setGeneration(Generation.TATTERED);
		meta.setAuthor(config.getString("fake_newspaper_author"));
		meta.addPage("tbw");
		// meta.setLore(null);

		meta.getPersistentDataContainer().set(BOOK_OF_LIES_KEY, PersistentDataType.BYTE, Byte.valueOf((byte) 0));
		book.setItemMeta(meta);

		ShapedRecipe recipe = new ShapedRecipe(BOOK_OF_LIES_KEY, book);

		recipe.shape("DDD", "DBD", "DDD");
		recipe.setIngredient('D', Material.DIAMOND);
		recipe.setIngredient('B', Material.BOOK);

		Bukkit.addRecipe(recipe);

		getServer().getPluginManager().registerEvents(new LiarLecternListener(this), this);
		getCommand("telllie").setExecutor(new CommandTelllLie(this));
	}
}
