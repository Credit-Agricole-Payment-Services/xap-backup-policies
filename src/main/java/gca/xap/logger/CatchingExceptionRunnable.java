package gca.xap.logger;

import lombok.AllArgsConstructor;

import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class CatchingExceptionRunnable implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(CatchingExceptionRunnable.class.getName());

	private final Runnable delegate;

	@Override
	public void run() {
		try {
			delegate.run();
		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, "Exception during execution of Runnable task.", e);
			e.printStackTrace(System.err);
		}
	}

}
