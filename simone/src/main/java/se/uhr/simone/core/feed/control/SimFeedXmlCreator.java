package se.uhr.simone.core.feed.control;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.uhr.simone.atom.feed.server.control.FeedXmlCreator;
import se.uhr.simone.core.control.SimoneConfiguration;
import se.uhr.simone.core.control.SimoneWorker;
import se.uhr.simone.core.feed.entity.SimFeedRepository;

@ApplicationScoped
public class SimFeedXmlCreator {

	private static final long DELAY = 2_000L;

	private static final Logger LOG = LoggerFactory.getLogger(SimFeedXmlCreator.class);

	@Inject
	SimoneConfiguration config;

	@Inject
	@SimoneWorker
	ManagedExecutor executor;

	@Inject
	FeedXmlCreator feedXmlCreator;

	@Inject
	SimFeedRepository feedRepository;

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		executor.submit(new SimFeedWorker());
	}

	class SimFeedWorker implements Callable<Void> {

		private boolean running = true;

		@Override
		public Void call() throws Exception {
			while (running) {
				try {
					Thread.sleep(DELAY);
					feedXmlCreator.createXmlForFeeds(feedRepository, config.getFeedBaseURI());
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					LOG.error("failed to create xml archive", e);
				}
			}

			return null;
		}
	}
}
