package net.mycorp.jimin.base.misc;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * from https://github.com/cfmobile/reactor-js/blob/master/reactor-js-core/src/main/java/com/pivotal/cf/mobile/ats/json/ScriptObjectMirrorSerializer.java
 */
@SuppressWarnings("restriction")
public class ScriptObjectMirrorSerializer extends StdSerializer<ScriptObjectMirror> {

	private static final long serialVersionUID = 8305081242161987686L;

	public ScriptObjectMirrorSerializer() {
		super(ScriptObjectMirror.class);
	}

	private StdDelegatingSerializer arraySerializer = null;

	private StdDelegatingSerializer objectSerializer = null;
	
	@Override
	public void serialize(ScriptObjectMirror value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		if (value.isArray()) {
			if (arraySerializer == null) {
				arraySerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObjectMirror.class,
						new StdConverter<ScriptObjectMirror, Collection<Object>>() {

							@Override
							public Collection<Object> convert(ScriptObjectMirror value) {
								return value.values();
							}

						}).createContextual(provider, null);

			}
			arraySerializer.serialize(value, jgen, provider);
		} else {
			if (objectSerializer == null) {
				objectSerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObjectMirror.class,
						new StdConverter<ScriptObjectMirror, Map<String, Object>>() {

							@Override
							public Map<String, Object> convert(ScriptObjectMirror value) {
								return value;
							}

						}).createContextual(provider, null);

			}
			objectSerializer.serialize(value, jgen, provider);
		}
	}

}