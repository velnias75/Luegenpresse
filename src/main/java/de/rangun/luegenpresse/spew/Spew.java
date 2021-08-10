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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class Spew {

	private final static Random rnd = new Random();

	private final static byte VBAR = '|';
	private final static byte SLASH = '/';
	private final static byte BSLASH = '\\';

	private final static ArrayList<Byte> COMMENT = new ArrayList<Byte>(2) {
		private static final long serialVersionUID = 7016931800472906831L;
		{
			add(BSLASH);
			add((byte) '*');
		}
	};

	private final static ArrayList<Byte> NullTags = new ArrayList<Byte>(2) {
		private static final long serialVersionUID = -5742816479367105985L;
		{
			add((byte) ' ');
			add((byte) '\0');
		}
	};

	private BufferedReader InFile;

	private ArrayList<Class> Class = new ArrayList<>();
	private ArrayList<Byte> InLine = new ArrayList<>();

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
		ArrayList<Byte> string;
		defn next;
	}

	private final static class Class implements Comparable<Class> {

		public int weight;

		defn list;
		ArrayList<Byte> name;
		ArrayList<Byte> tags;

		@Override
		public int compareTo(Class o) {
			return (new String(ArrayUtils.toPrimitive(name.toArray(new Byte[0]))))
					.compareTo(new String(ArrayUtils.toPrimitive(o.name.toArray(new Byte[0]))));
		}
	}

	private void readtext() throws IOException, SpewException {

		Class cp;
		defn dp;
		defn update;

		cp = new Class();
		Class.add(cp);

		readline();

		if (InLine.get(0) != '%')
			throw new SpewException("Class definition expected at: ", InLine);

		while (InLine.get(1) != '%') {

			setup(cp);
			readline();

			if (InLine.get(0) == '%') {
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

			cp = new Class();
			Class.add(cp);

			update = null;
		}

		Class.remove(Class.size() - 1);
		Class.trimToSize();

		Collections.sort(Class);
	}

	private boolean nextLine() throws IOException {
		readline();
		return InLine.get(0) != '%';
	}

	private void readline() throws IOException {

		InLine.clear();

		do {

			final String line = InFile.readLine();
			int idx = 0;

			if (line == null) {

				InLine.add((byte) '%');
				InLine.add((byte) '%');
				InLine.add((byte) '\0');

			} else {
				for (byte b : line.getBytes(StandardCharsets.UTF_8)) {
					InLine.add(b);
				}
			}

			InLine.add((byte) '\0');

			if ((idx = Collections.indexOfSubList(InLine, COMMENT)) != -1) {

				if (idx == 0) {
					InLine.clear();
				} else {
					InLine.set(idx, (byte) '\0');
				}
			}

		} while (InLine.isEmpty());
	}

	private ArrayList<Byte> save(List<Byte> str) {
		return new ArrayList<>(str);
	}

	private void setup(Class cp) throws SpewException {

		int p = 1;
		int p2 = 0;
		final ArrayList<Byte> temp = new ArrayList<>(100);

		while (InLine.get(p) == ' ')
			++p;

		if (!isalnum(InLine, p))
			throw new SpewException("Bad class header: ", InLine);

		do {
			temp.add(p2++, InLine.get(p));
		} while (isalnum(InLine, p++));

		temp.set(--p2, (byte) '\0');

		cp.weight = 0;
		cp.name = save(temp);
		cp.name.trimToSize();
		cp.list = null;
		cp.tags = NullTags;

		--p;

		for (;;) {
			switch (InLine.get(p++)) {
			case '\0':
				return;
			case ' ':
				break;
			case '{':

				if (!NullTags.equals(cp.tags))
					baddec();

				p2 = 0;
				temp.set(p2++, (byte) ' ');

				while (InLine.get(p) != '}') {

					if (!isalnum(InLine, p))
						baddec();

					temp.set(p2++, InLine.get(p++));
				}

				++p;
				temp.set(p2, (byte) '\0');

				cp.tags = save(temp);
				cp.tags.trimToSize();

				break;
			default:
				baddec();
			}
		}
	}

	private defn process() throws SpewException, IOException {

		final ArrayList<Byte> stuff = new ArrayList<>();
		final defn dp = new defn();

		int c;
		int p = 0;

		if (InLine.get(p) == '(') {

			while (InLine.get(++p) == ' ') {
			}

			if (!isdigit(InLine.get(p)))
				badweight();

			c = InLine.get(p) - '0';

			while (isdigit(InLine.get(++p)))
				c = c * 10 + (InLine.get(p) - '0');

			while (InLine.get(p) == ' ')
				++p;

			if (InLine.get(p) != ')')
				badweight();

			++p;
			dp.cumul = c;

		} else {
			dp.cumul = 1;
		}

		while ((c = InLine.get(p++)) != '\0') {
			switch (c) {
			case BSLASH:

				stuff.add(BSLASH);

				if (isalnum(InLine, p)) {

					do {
						stuff.add(InLine.get(p++));
					} while (isalnum(InLine, p));

					stuff.add(SLASH);

					if (InLine.get(p) == SLASH) {

						++p;

						if (!isalnum(InLine, p) && InLine.get(p) != ' ' && InLine.get(p) != '&') {
							stuff.add((byte) ' ');
						} else {
							stuff.add(InLine.get(p++));
						}

					} else {
						stuff.add((byte) ' ');
					}

				} else {

					stuff.add(InLine.get(p));

					if (InLine.get(p) != '\0') {
						++p;
					} else {
						readline();
						p = 0;
					}
				}

				break;
			default:
				stuff.add((byte) c);
				break;
			}
		}

		stuff.add((byte) '\0');
		dp.string = save(stuff);
		dp.string.trimToSize();

		return dp;

	}

	public String getHeadline() throws SpewException {

		List<Byte> sb = new ArrayList<>();

		display(sb, Arrays.asList(ArrayUtils.toObject("MAIN/ ".getBytes())), ' ');

		return new String(ArrayUtils.toPrimitive(sb.toArray(new Byte[0])));
	}

	@SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "false positive")
	private void display(List<Byte> sb, List<Byte> s, int deftag) throws SpewException {

		int i;
		int c;
		int p;
		int variant;
		int incurly;
		int writing;

		defn dp;
		Class cp = lookup(s);

		if (cp == null) {
			throw new SpewException("Class lookup failed for: ", s);
		}

		c = s.get(s.indexOf(SLASH) + 1);

		if (c != '&')
			deftag = c;

		p = cp.tags.indexOf((byte) deftag);

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
			switch ((c = dp.string.get(p++))) {
			case '\0':
				return;
			case BSLASH:
				if ((c = dp.string.get(p++)) == '\0')
					return;
				else if (c == '!') {
					sb.add((byte) '\n');
				} else if (isalnum(dp.string, p - 1)) {

					if (writing == 1) {

						display(sb, dp.string.subList(p - 1, dp.string.size() - 1), deftag);

						while (dp.string.get(p) != SLASH)
							++p;

						p += 2;

					} else {
						if (writing == 1)
							sb.add((byte) c);
					}
				}

				break;

			case '{':

				if (incurly == 0) {
					incurly = 1;
					writing = variant == 0 ? 1 : 0;
				} else {
					if (writing == 1)
						sb.add((byte) '{');
				}

				break;

			case VBAR:

				if (incurly != 0) {
					writing = (variant == incurly++) ? 1 : 0;
				} else {
					if (writing == 1)
						sb.add((byte) VBAR);
				}

				break;

			case '}':

				if (incurly != 0) {
					writing = 1;
					incurly = 0;
				} else {
					sb.add((byte) '}');
				}

				break;

			default:
				if (writing == 1)
					sb.add((byte) c);
			}
		}

	}

	private int ROLL(final int n) {
		return (int) ((((long) rnd.nextInt() & 0x7ffffff) >> 5) % (n));
	}

	private Class lookup(final List<Byte> str) {

		int comp;
		int tryy;
		int first = 0;
		int last = Class.size() - 1;

		while (first <= last) {

			tryy = (first + last) >> 1;
			comp = namecomp(str, Class.get(tryy).name);

			if (comp == 0)
				return Class.get(tryy);

			if (comp > 0) {
				first = tryy + 1;
			} else {
				last = tryy - 1;
			}

		}

		return null;
	}

	private int namecomp(final List<Byte> a, final List<Byte> b) {

		int ac;
		int ap = 0;
		int bp = 0;

		for (;;) {

			ac = a.get(ap++);

			if (ac == SLASH)
				ac = '\0';

			if (ac < b.get(bp))
				return -1;

			if (ac > b.get(bp++))
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

	private boolean isalnum(List<Byte> list, int idx) {
		return Character.isDigit((char) ((byte) list.get(idx)))
				|| Character.isAlphabetic((char) ((byte) list.get(idx)));
	}
}
