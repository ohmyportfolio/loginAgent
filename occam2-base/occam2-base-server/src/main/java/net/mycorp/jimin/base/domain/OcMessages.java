package net.mycorp.jimin.base.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("messages")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "messages" })
public class OcMessages {

	@XStreamAsAttribute
	private String locale;

	private List<OcMessage> messages;

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public List<OcMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<OcMessage> messages) {
		this.messages = messages;
	}
	
}
