package org.javastack.log4j.simplejson;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.util.Strings;

@Plugin(name = "SimpleJSONLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public final class SimpleJSONLayout extends AbstractStringLayout {
	private static final String CONTENT_TYPE = "application/json";
	private static final String DEFAULT_HEADER = "[";
	private static final String DEFAULT_FOOTER = "]";
	private static final String DEFAULT_EOL = "\r\n";
	private static final String COMPACT_EOL = Strings.EMPTY;

	/**
	 * Current SimpleJSONLayout version
	 */
	public static final int LAYOUT_VERSION = 1;

	private final long layoutStartTime;
	private final AtomicLong layoutSequence;
	private final Interpolator interpolator;
	private final KeyValuePair[] additionalFields;
	private final boolean locationInfo;
	private final boolean properties;
	private final boolean complete;
	private final String eol;

	protected SimpleJSONLayout(final Configuration config, final boolean locationInfo,
			final boolean properties, final boolean complete, final boolean eventEol,
			final String headerPattern, final String footerPattern, final Charset charset,
			final KeyValuePair[] additionalFields) {
		super(config, charset, //
				PatternLayout.newSerializerBuilder()
					.setConfiguration(config)
					.setReplace(null)
					.setPattern(headerPattern)
					.setDefaultPattern(DEFAULT_HEADER)
					.setPatternSelector(null)
					.setAlwaysWriteExceptions(false)
					.setNoConsoleNoAnsi(false)
					.build(),
				PatternLayout.newSerializerBuilder()
					.setConfiguration(config)
					.setReplace(null)
					.setPattern(footerPattern)
					.setDefaultPattern(DEFAULT_FOOTER)
					.setPatternSelector(null)
					.setAlwaysWriteExceptions(false)
					.setNoConsoleNoAnsi(false)
					.build());
		this.locationInfo = locationInfo;
		this.properties = properties;
		this.complete = complete;
		this.additionalFields = additionalFields;
		this.layoutStartTime = System.currentTimeMillis();
		this.layoutSequence = new AtomicLong();
		this.interpolator = new Interpolator(new MapLookup(getConfiguration().getProperties()),
				getConfiguration().getPluginPackages());
		this.eol = !eventEol ? COMPACT_EOL : DEFAULT_EOL;
	}

	@Override
	public Map<String, String> getContentFormat() {
		final Map<String, String> result = super.getContentFormat();
		result.put("version", String.valueOf(LAYOUT_VERSION));
		return result;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE + "; charset=" + this.getCharset();
	}

	@Override
	public byte[] getHeader() {
		if (!this.complete) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		final String str = serializeToString(getHeaderSerializer());
		if (str != null) {
			buf.append(str);
		}
		buf.append(this.eol);
		return getBytes(buf.toString());
	}

	@Override
	public byte[] getFooter() {
		if (!this.complete) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		buf.append(this.eol);
		final String str = serializeToString(getFooterSerializer());
		if (str != null) {
			buf.append(str);
		}
		buf.append(this.eol);
		return getBytes(buf.toString());
	}

	@Override
	public String toSerializable(final LogEvent event) {
		try {
			return format(convertMutableToLog4jEvent(event));
		} finally {
			markEvent();
		}
	}

	private static LogEvent convertMutableToLog4jEvent(final LogEvent event) {
		// TODO (from JsonLayout): Need to set up the same filters for MutableLogEvent but don't know how...
		return ((event instanceof MutableLogEvent) ? ((MutableLogEvent) event).createMemento() : event);
	}

	private final String format(final LogEvent event) {
		final StringBuilder sb = getStringBuilder();
		if (complete && eventCount > 0) {
			sb.append(", ");
		}
		sb.append('{');
		// Internal Info
		json(sb, "layout.version", LAYOUT_VERSION);
		json(sb, "layout.start", layoutStartTime);
		json(sb, "layout.sequence", layoutSequence.incrementAndGet());
		// Basic Info
		json(sb, "timestamp", event.getTimeMillis());
		json(sb, "thread", event.getThreadName());
		json(sb, "threadId", event.getThreadId());
		json(sb, "level", event.getLevel().toString());
		json(sb, "logger", event.getLoggerName());
		// Caller info
		if (locationInfo) {
			final StackTraceElement source = event.getSource();
			json(sb, "source");
			sb.append('{');
			json(sb, "class", source.getClassName());
			json(sb, "method", source.getMethodName());
			json(sb, "file", source.getFileName());
			json(sb, "line", source.getLineNumber());
			sb.setLength(sb.length() - 1);
			sb.append('}').append(',');
		}
		// Diagnostic Context
		if (properties) {
			if (!event.getContextStack().isEmpty()) {
				json(sb, "ndc", event.getContextStack().asList());
			}
			if (!event.getContextData().isEmpty()) {
				json(sb, "mdc", event.getContextData().toMap());
			}
		}
		// Additional Fields
		for (int i = 0; i < additionalFields.length; i++) {
			final KeyValuePair kv = additionalFields[i];
			final String key = kv.getKey();
			final String value = kv.getValue();
			final String iv = interpolator.lookup(event, value);
			if (iv != null) {
				json(sb, (key != null) ? key : value, iv);
			}
		}
		// Message
		json(sb, "msg", event.getMessage().getFormattedMessage());
		// Exceptions
		if (event.getThrownProxy() != null) {
			final ThrowableProxy throwableInfo = event.getThrownProxy();
			final Throwable t = throwableInfo.getThrowable();
			final String exClass = t.getClass().getCanonicalName();
			if (exClass != null) {
				json(sb, "exception", exClass);
			}
			final String exMsg = t.getMessage();
			if (exMsg != null) {
				json(sb, "cause", exMsg);
			}
			// TODO: Change pure string to complex list/maps of stacktraces?
			final String stackTrace = throwableInfo.getExtendedStackTraceAsString();
			if (stackTrace != null) {
				json(sb, "stacktrace", stackTrace);
			}
		}
		sb.setLength(sb.length() - 1);
		sb.append('}').append(eol);
		return sb.toString();
	}

	private final StringBuilder json(final StringBuilder sb, final String key, final List<String> value) {
		json(sb, key).append('[');
		if ((value != null) && (value.size() > 0)) {
			for (int i = 0; i < value.size(); i++) {
				sb.append('"');
				EncoderJSON.escapeJSON(value.get(i), sb);
				sb.append('"').append(',');
			}
			sb.setLength(sb.length() - 1);
		}
		return sb.append(']').append(',');
	}

	private final StringBuilder json(final StringBuilder sb, final String key, final Map<?, ?> m) {
		json(sb, key).append('{');
		if ((m != null) && !m.isEmpty()) {
			for (final Entry<?, ?> e : m.entrySet()) {
				final String sk = ((e.getKey() != null) ? e.getKey().toString() : "");
				final String sv = ((e.getValue() != null) ? e.getValue().toString() : "");
				json(sb, sk, sv);
			}
			sb.setLength(sb.length() - 1);
		}
		return sb.append('}').append(',');
	}

	private final StringBuilder json(final StringBuilder sb, final String key) {
		sb.append('"');
		if (key != null) {
			EncoderJSON.escapeJSON(key, sb);
		}
		return sb.append('"').append(':');
	}

	private final StringBuilder json(final StringBuilder sb, final String key, final String value) {
		json(sb, key).append('"');
		if (value != null) {
			EncoderJSON.escapeJSON(value, sb);
		}
		return sb.append('"').append(',');
	}

	private final StringBuilder json(final StringBuilder sb, final String key, final long value) {
		return json(sb, key).append(value).append(',');
	}

	@PluginFactory
	public static SimpleJSONLayout createLayout( //
			@PluginConfiguration final Configuration config, //
			@PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo, //
			@PluginAttribute(value = "properties", defaultBoolean = true) final boolean properties, //
			@PluginAttribute(value = "complete", defaultBoolean = false) final boolean complete, //
			@PluginAttribute(value = "eventEol", defaultBoolean = true) final boolean eventEol, //
			@PluginAttribute(value = "header", defaultString = DEFAULT_HEADER) final String headerPattern, //
			@PluginAttribute(value = "footer", defaultString = DEFAULT_FOOTER) final String footerPattern, //
			@PluginAttribute(value = "charset", defaultString = "US-ASCII") final Charset charset, //
			@PluginElement("AdditionalField") final KeyValuePair[] additionalFields) {
		return new SimpleJSONLayout(config, locationInfo, properties, complete, eventEol, headerPattern,
				footerPattern, charset, additionalFields);
	}
}
