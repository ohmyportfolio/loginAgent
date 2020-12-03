package net.mycorp.jimin.base.core;

public class OccamException extends RuntimeException {

	public OccamException(String message, Object... params) {
		super(String.format(message, params));
	}

	public OccamException(Throwable e) {
		super(e);
	}

	public OccamException(Throwable e, String message, Object... params) {
		super(String.format(message, params), e);
	}

	private static final long serialVersionUID = -8568191636274214243L;

}
