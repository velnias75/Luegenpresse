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

package de.rangun.luegenpresse.spew;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.primitives.Bytes;

public final class Spew {

	private final static int MAXCLASS = 300;
	private final static int MAXLINE = 256;
	private final static int MAXDEF = 1000;

	private final static byte BSLASH = '\\';
	private final static byte[] COMMENT = { BSLASH, '*' };

	private BufferedReader InFile;

	private final static byte[] NullTags = { ' ', '\0' };

	private Class[] Class;
	private int Classes;
	private byte[] InLine = new byte[MAXLINE];

	public static void main(String[] args) throws IOException, SpewException {
		System.out.println("SPEW!");

		Spew spew = new Spew();
	}

	public Spew() throws IOException, SpewException {

		InFile = new BufferedReader(
				new InputStreamReader(this.getClass().getResourceAsStream("/headline"), StandardCharsets.UTF_8));
		readtext();
	}

	private final static class defn {
		int cumul;
		byte[] string;
		defn next;
	}

	private final static class Class {
		public int weight;
		defn[] list;
		byte[] name;
		byte[] tags;
	}

	private void readtext() throws IOException, SpewException {

		Class cp;
		defn dp;
		defn update;

		Class = new Class[MAXCLASS];

		for (int i = 0; i < Class.length; ++i)
			Class[i] = new Class();

		Classes = 0;

		cp = Class[0];
		readline();

		if (InLine[0] != '%')
			throw new SpewException("Class definition expected at: ", InLine);

		while (InLine[1] != '%') {

			if (Classes == MAXCLASS)
				throw new SpewException("Too many classes -- max = ", MAXCLASS);

			setup(cp);
			readline();

			if (InLine[0] == '%') {
				throw new SpewException("Expected class instance at: ", InLine);
			}

			do {

			} while (nextLine());
		}
	}

	private boolean nextLine() throws IOException {
		readline();
		return InLine[0] != '%';
	}

	private void readline() throws IOException {

		do {

			final String line = InFile.readLine();
			int idx = 0;

			if (line == null) {

				InLine[0] = InLine[1] = '%';
				InLine[2] = '\0';

			} else {

				for (byte b : line.getBytes()) {
					InLine[idx++] = b;
				}
			}

			InLine[idx] = '\0';

			if ((idx = Bytes.indexOf(InLine, COMMENT)) != -1)
				InLine[idx] = '\0';

		} while (InLine[0] == '\0');
	}

	private void save(Class cp, byte[] str) {

		cp.name = new byte[str.length];

		for (int i = 0; i < str.length; ++i) {
			cp.name[i] = str[i];
		}
	}

	private void setup(Class cp) throws SpewException {

		int p = 1;
		int p2 = 0;
		byte[] temp = new byte[100];

		while (InLine[p] == ' ')
			++p;

		if (!isalnum(InLine[p]))
			throw new SpewException("Bad class header: ", InLine);

		do {
			temp[p2++] = InLine[p];
		} while (isalnum(InLine[p++]));

		temp[--p2] = '\0';

		cp.weight = 0;
		save(cp, temp);
		cp.list = null;
		cp.tags = NullTags;

		--p;

		for (;;) {
			switch (InLine[p++]) {
			case '\0':
				return;
			case ' ':
				break;
			case '{':

				if (!Arrays.equals(cp.tags, NullTags))
					baddec();

				p2 = 0;
				temp[p2++] = ' ';

				while (InLine[p] != '}') {

					if (!isalnum(InLine[p]))
						baddec();

					temp[p2++] = InLine[p++];
				}

				++p;
				temp[p2] = '\0';

				save(cp, temp);
				break;
			default:
				baddec();
			}
		}
	}

	private void baddec() throws SpewException {
		throw new SpewException("Bad class header: ", InLine);
	}

	private boolean isalnum(byte c) {
		return Character.isLetterOrDigit((char) c);
	}
}
