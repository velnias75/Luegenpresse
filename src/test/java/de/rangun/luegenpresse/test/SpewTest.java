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

package de.rangun.luegenpresse.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.rangun.luegenpresse.spew.DefnStringProvider;
import de.rangun.luegenpresse.spew.Spew;
import de.rangun.luegenpresse.spew.SpewException;

public class SpewTest {

	private Spew spew;
	private DefnStringProvider offline_dsp = new DefnStringProvider() {

		@Override
		public List<Byte> getString(int rnd) {
			return null;
		}
	};

	@Before
	public void setUp() throws Exception {

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("headline").getFile());

		spew = Spew.getInstance(new File(file.getAbsolutePath()), offline_dsp, Long.valueOf(1L));
	}

	@Test
	public void test() throws SpewException {
		assertEquals("\"I Saw Groucho Marx Alive and Well in Missouri\" Says Game Show Host.\n", spew.getHeadline());
	}

}
