package se.uhr.simone.core.feed.control;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uhr.simone.atom.feed.server.control.FeedCreator;
import se.uhr.simone.core.control.SimoneWorker;
import se.uhr.simone.core.feed.entity.SimFeedRepository;
import se.uhr.simone.extension.api.SimoneStartupEvent;

@ApplicationScoped
public class SimFeedCreator {

	private static final long DELAY = 2_000L;

	private static final Logger LOG = LoggerFactory.getLogger(SimFeedCreator.class);

	@Inject
	@SimoneWorker
	ManagedExecutor executor;

	@Inject
	FeedCreator feedCreator;

	@Inject
	SimFeedRepository feedRepository;

	public void init(@Observes SimoneStartupEvent ev) {
		executor.submit(new SimFeedWorker());
	}

	class SimFeedWorker implements Callable<Void> {

		private boolean running = true;

		@Override
		public Void call() throws Exception {
			while (running) {
				try {
					feedCreator.connectEntrysToFeeds(feedRepository);
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					LOG.error("failed to create feed", e);
				}
			}

			return null;
		}
	}
}
