package se.uhr.simone.core.control.filemonitor;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uhr.simone.core.control.SimoneWorker;

@ApplicationScoped
public class DirectoryMonitorScheduler {

	private static final long DELAY = 1_000L;

	private static final Logger LOG = LoggerFactory.getLogger(DirectoryMonitorScheduler.class);

	@Inject
	@SimoneWorker
	ManagedExecutor executor;

	@Inject
	private DirectoryMonitor monitor;

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		executor.submit(new DirectoryMonitorWorker());
	}

	class DirectoryMonitorWorker implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			while (monitor.isActive()) {
				try {
					Thread.sleep(DELAY);
					monitor.runAvailableJobs();
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					LOG.error("dropin directory monitoring failed", e);
				}
			}

			return null;
		}
	}
}
