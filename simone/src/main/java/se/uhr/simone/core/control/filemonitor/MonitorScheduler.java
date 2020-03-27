package se.uhr.simone.core.control.filemonitor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uhr.simone.core.control.SimoneWorker;

@ApplicationScoped
public class MonitorScheduler {

	private static final long DELAY = 1000L;

	private static final Logger LOG = LoggerFactory.getLogger(MonitorScheduler.class);

	@Inject
	@SimoneWorker
	ManagedExecutor executor;

	@Inject
	private DirectoryMonitor monitor;

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		executor.submit(new DirectoryMonitorWorker());
	}

	class DirectoryMonitorWorker implements Runnable {

		private boolean running = true;

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(DELAY);
					monitor.runAvailableJobs();
				} catch (Exception e) {
					LOG.error("Failed to create feed", e);
				}
			}
		}
	}
}
