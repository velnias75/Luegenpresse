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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.google.common.primitives.Bytes;

public final class Spew {

	private final static Random rnd = new Random();

	private final static int MAXCLASS = 300;
	private final static int MAXLINE = 256;
	private final static int MAXDEF = 1000;

	private final static byte VBAR = '|';
	private final static byte SLASH = '/';
	private final static byte BSLASH = '\\';
	private final static byte[] COMMENT = { BSLASH, '*' };

	private BufferedReader InFile;

	private final static byte[] NullTags = { ' ', '\0' };

	private Class[] Class;
	private int Classes;
	private byte[] InLine = new byte[MAXLINE];

	public Spew(final File in) throws IOException, SpewException {
		InFile = new BufferedReader(new FileReader(in));
		readtext();
	}

	public Spew(final File in, final long seed) throws IOException, SpewException {
		InFile = new BufferedReader(new FileReader(in));
		rnd.setSeed(seed);
		readtext();
	}

	private final static class defn {
		int cumul;
		byte[] string;
		defn next;
	}

	private final static class Class implements Comparable<Class> {

		public int weight;
		defn list;
		byte[] name;
		byte[] tags;

		@Override
		public int compareTo(Class o) {
			return (new String(name)).compareTo(new String(o.name));
		}
	}

	private void readtext() throws IOException, SpewException {

		Class cp;
		defn dp;
		defn update;
		int ci = 0;

		Class = new Class[MAXCLASS];

		for (int i = 0; i < Class.length; ++i)
			Class[i] = new Class();

		Classes = 0;

		cp = Class[ci];
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

			update = null;

			do {

				dp = process();

				if (cp.list == null) {
					cp.list = dp;
				} else {
					update.next = dp;
				}

				cp.weight += dp.cumul;
				dp.cumul = cp.weight;

				update = dp;

			} while (nextLine());

			++Classes;
			cp = Class[++ci];

			update = null;
		}

		Arrays.sort(Class, 0, Classes);
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

			if ((idx = Bytes.indexOf(InLine, COMMENT)) != -1) {
				InLine[idx] = '\0';
			}

		} while (InLine[0] == '\0');
	}

	private byte[] save(byte[] str) {

		final byte[] b = new byte[str.length];

		for (int i = 0; i < str.length; ++i) {
			b[i] = str[i];
		}

		return b;
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
		cp.name = save(temp);
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

				cp.tags = save(temp);
				break;
			default:
				baddec();
			}
		}
	}

	private defn process() throws SpewException, IOException {

		byte[] stuff = new byte[MAXDEF];

		final defn dp = new defn();

		int c;
		int p = 0;
		int pout = 0;

		if (InLine[p] == '(') {

			while (InLine[++p] == ' ') {
			}

			if (!isdigit(InLine[p]))
				badweight();

			c = InLine[p] - '0';

			while (isdigit(InLine[++p]))
				c = c * 10 + (InLine[p] - '0');

			while (InLine[p] == ' ')
				++p;

			if (InLine[p] != ')')
				badweight();

			++p;
			dp.cumul = c;

		} else {
			dp.cumul = 1;
		}

		while ((c = InLine[p++]) != '\0') {
			switch (c) {
			case BSLASH:

				stuff[pout++] = BSLASH;

				if (isalnum(InLine[p])) {

					do {
						stuff[pout++] = InLine[p++];
					} while (isalnum(InLine[p]));

					stuff[pout++] = SLASH;

					if (InLine[p] == SLASH) {

						++p;

						if (!isalnum(InLine[p]) && InLine[p] != ' ' && InLine[p] != '&') {
							stuff[pout++] = ' ';
						} else {
							stuff[pout++] = InLine[p++];
						}

					} else {
						stuff[pout++] = ' ';
					}

				} else {

					stuff[pout++] = InLine[p];

					if (InLine[p] != '\0') {
						++p;
					} else {
						--pout;
						readline();
						p = 0;
					}
				}

				break;
			default:
				stuff[pout++] = (byte) c;
				break;
			}
		}

		stuff[pout] = '\0';
		dp.string = save(stuff);

		return dp;

	}

	public String getHeadline() {

		final StringBuilder sb = new StringBuilder();

		display(sb, "MAIN/ ".getBytes(), ' ');

		return sb.toString();
	}

	private void display(StringBuilder sb, final byte[] s, int deftag) {

		int i;
		int c;
		int p;
		int variant;
		int incurly;
		int writing;

		defn dp;

		Class cp = lookup(s);

		c = s[Bytes.indexOf(s, SLASH) + 1];

		if (c != '&')
			deftag = c;

		p = Bytes.indexOf(cp.tags, (byte) deftag);

		if (p == -1) {
			variant = 0;
			deftag = ' ';
		} else {
			variant = p;
		}

		i = ROLL(cp.weight);
		dp = cp.list;

		while (dp.cumul <= i) {
			dp = dp.next;
		}

		incurly = 0;
		writing = 1;
		p = 0;

		for (;;) {
			switch ((c = dp.string[p++])) {
			case '\0':
				return;
			case BSLASH:
				if ((c = dp.string[p++]) == '\0')
					return;
				else if (c == '!') {
					sb.append('\n');
				} else if (isalnum((byte) c)) {

					if (writing == 1) {

						display(sb, new String(dp.string).substring(p - 1).getBytes(), deftag);

						while (dp.string[p] != SLASH)
							++p;

						p += 2;

					} else {
						if (writing == 1)
							sb.append((char) c);
					}
				}

				break;

			case '{':

				if (incurly == 0) {
					incurly = 1;
					writing = variant == 0 ? 1 : 0;
				} else {
					if (writing == 1)
						sb.append('{');
				}

				break;

			case VBAR:

				if (incurly != 0) {
					writing = (variant == incurly++) ? 1 : 0;
				} else {
					if (writing == 1)
						sb.append((char) VBAR);
				}

				break;

			case '}':

				if (incurly != 0) {
					writing = 1;
					incurly = 0;
				} else {
					sb.append('}');
				}

				break;

			default:
				if (writing == 1)
					sb.append((char) c);
			}
		}

	}

	private int ROLL(final int n) {
		return (int) ((((long) rnd.nextInt() & 0x7ffffff) >> 5) % (n));
	}

	private Class lookup(final byte[] str) {

		int comp;
		int tryy;
		int first = 0;
		int last = Classes - 1;

		while (first <= last) {

			tryy = (first + last) >> 1;
			comp = namecomp(str, Class[tryy].name);

			if (comp == 0)
				return Class[tryy];

			if (comp > 0) {
				first = tryy + 1;
			} else {
				last = tryy - 1;
			}

		}

		return null;
	}

	private int namecomp(final byte[] a, final byte[] b) {

		int ac;
		int ap = 0;
		int bp = 0;

		for (;;) {

			ac = a[ap++];

			if (ac == SLASH)
				ac = '\0';

			if (ac < b[bp])
				return -1;

			if (ac > b[bp++])
				return 1;

			if (ac == '\0')
				return 0;
		}
	}

	private void baddec() throws SpewException {
		throw new SpewException("Bad class header: ", InLine);
	}

	private void badweight() throws SpewException {
		throw new SpewException("Bad line weight: ", InLine);
	}

	private boolean isdigit(byte c) {
		return Character.isDigit((char) c);
	}

	private boolean isalnum(byte c) {
		return Character.isLetterOrDigit((char) c);
	}
}
