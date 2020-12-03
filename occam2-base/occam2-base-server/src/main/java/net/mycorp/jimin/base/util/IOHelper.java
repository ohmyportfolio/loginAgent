package net.mycorp.jimin.base.util;

import java.io.Closeable;
import java.io.IOException;

import net.mycorp.jimin.base.core.OccamException;

public class IOHelper {

	public static void close(Closeable closeable) {
		if (closeable != null)
			try {
				closeable.close();
			} catch (IOException e) {
				new OccamException(e);
			}
	}
}
