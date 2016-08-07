package org.javastack.log4j.simplejson;

import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;

/**
 * Returns the event's rendered message in a StringBuilder.
 */
@Plugin(name = "json", category = PatternConverter.CATEGORY)
@ConverterKeys({
	"json"
})
public final class SimpleJSONPatternConverter extends LogEventPatternConverter {

	private final List<PatternFormatter> formatters;

	/**
	 * Private constructor.
	 * 
	 * @param formatters The PatternFormatters to generate the text to manipulate.
	 */
	private SimpleJSONPatternConverter(final List<PatternFormatter> formatters) {
		super("json", "json");
		this.formatters = formatters;
	}

	/**
	 * Obtains an instance of pattern converter.
	 * 
	 * @param config The Configuration.
	 * @param options options, may be null.
	 * @return instance of pattern converter.
	 */
	public static SimpleJSONPatternConverter newInstance(final Configuration config, final String[] options) {
		if (options.length != 1) {
			LOGGER.error("Incorrect number of options on json. Expected 1, received " + options.length);
			return null;
		}
		if (options[0] == null) {
			LOGGER.error("No pattern supplied on json");
			return null;
		}
		final PatternParser parser = PatternLayout.createPatternParser(config);
		final List<PatternFormatter> formatters = parser.parse(options[0]);
		return new SimpleJSONPatternConverter(formatters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LogEvent event, final StringBuilder toAppendTo) {
		final StringBuilder buf = new StringBuilder();
		for (final PatternFormatter formatter : formatters) {
			formatter.format(event, buf);
		}
		EncoderJSON.escapeJSON(buf, toAppendTo);
	}
}
