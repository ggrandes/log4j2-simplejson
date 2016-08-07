package org.javastack.log4j.simplejson;

public class EncoderJSON {
	/** A table of hex digits */
	private static final char[] hexDigit = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(final int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	public static final String escapeJSON(final CharSequence in) {
		final StringBuilder sb = new StringBuilder(in.length() + 256);
		escapeJSON(in, sb);
		return sb.toString();
	}

	public static final void escapeJSON(final CharSequence in, final StringBuilder sb) {
		// https://tools.ietf.org/html/rfc7159
		final int len = in.length();
		for (int i = 0; i < len; i++) {
			final char c = in.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '/':
					sb.append("\\/");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					if (((c < 0x0020) || (c > 0x007e))) {
						sb.append("\\u");
						sb.append(toHex(c >> 12));
						sb.append(toHex(c >> 8));
						sb.append(toHex(c >> 4));
						sb.append(toHex(c));
					} else {
						sb.append(c);
					}
			}
		}
	}
}
