package se.uhr.simone.atom.feed.server.entity;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import se.uhr.simone.atom.feed.utils.UniqueIdentifier;

public abstract class FeedRepository {

	private AtomFeedDAO atomFeedDAO;
	private AtomEntryDAO atomEntryDAO;
	private AtomCategoryDAO atomCategoryDAO;

	public abstract DataSource getDataSource();

	@PostConstruct
	public void setupDao() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		atomFeedDAO = createAtomFeedDAO(jdbcTemplate);
		atomEntryDAO = createAtomEntryDAO(jdbcTemplate);
		atomCategoryDAO = createAtomCategoryDAO(jdbcTemplate);
	}

	protected AtomFeedDAO createAtomFeedDAO(JdbcTemplate jdbcTemplate) {
		return new AtomFeedDAO(jdbcTemplate);
	}

	protected AtomEntryDAO createAtomEntryDAO(JdbcTemplate jdbcTemplate) {
		return new AtomEntryDAO(jdbcTemplate);
	}

	protected AtomCategoryDAO createAtomCategoryDAO(JdbcTemplate jdbcTemplate) {
		return new AtomCategoryDAO(jdbcTemplate);
	}

	public void saveAtomFeed(AtomFeed atomFeed) {
		if (atomFeedDAO.exists(atomFeed.getId())) {
			atomFeedDAO.update(atomFeed);
		} else {
			atomFeedDAO.insert(atomFeed);
		}

		for (AtomEntry atomEntry : atomFeed.getEntries()) {
			atomEntry.setFeedId(atomFeed.getId());
			saveAtomEntry(atomEntry);
		}
	}

	public void saveAtomEntry(AtomEntry atomEntry) {
		if (atomEntryDAO.exists(atomEntry.getAtomEntryId())) {
			atomEntryDAO.update(atomEntry);
		} else {
			atomEntryDAO.insert(atomEntry);
		}

		for (AtomCategory atomCategory : atomEntry.getAtomCategories()) {
			if (!atomCategoryDAO.isConnected(atomCategory, atomEntry.getAtomEntryId())) {
				atomCategoryDAO.connectEntryToCategory(atomEntry.getAtomEntryId(), atomCategory);
			}
		}
	}

	public void saveAtomFeedXml(long feedId, String xml) {
		atomFeedDAO.saveAtomFeedXml(feedId, xml);
	}

	public AtomFeed getFeedById(long id) {
		AtomFeed atomFeed = null;
		try {
			atomFeed = atomFeedDAO.fetchBy(id);
		} catch (EmptyResultDataAccessException e) {
			return atomFeed;
		}
		atomFeed.setEntries(getEntriesForFeed(atomFeed));
		return atomFeed;
	}

	/**
	 * Will return the "recent" {@link AtomFeed} or null if not existing.
	 * 
	 * @return "recent" {@link AtomFeed} or null if not existing.
	 */
	public AtomFeed getRecentFeed() {
		AtomFeed recent = atomFeedDAO.fetchRecent();
		recent.setEntries(getEntriesForFeed(recent));
		return recent;
	}

	/**
	 * Will return all {@link AtomEntry}s that are not connected to a {@link AtomFeed}.
	 * 
	 * @return all {@link AtomEntry}s that are not connected to a {@link AtomFeed}
	 */
	public List<AtomEntry> getEntriesNotConnectedToFeed() {
		List<AtomEntry> entriesNotConnectedToFeed = atomEntryDAO.getEntriesNotConnectedToFeed();
		for (AtomEntry atomEntry : entriesNotConnectedToFeed) {
			List<AtomCategory> categoriesForAtomEntry = atomCategoryDAO.getCategoriesForAtomEntry(atomEntry.getAtomEntryId());
			atomEntry.setAtomCategories(categoriesForAtomEntry);
		}
		return entriesNotConnectedToFeed;
	}

	/**
	 * Will return a list of {@link AtomFeed}s that has no xml.
	 * 
	 * @return all {@link AtomFeed}s that has no xml.
	 */
	public List<AtomFeed> getFeedsWithoutXml() {
		List<AtomFeed> feedsWithoutXml = atomFeedDAO.getFeedsWithoutXml();
		for (AtomFeed atomFeed : feedsWithoutXml) {
			atomFeed.setEntries(getEntriesForFeed(atomFeed));
		}
		return feedsWithoutXml;
	}

	public UniqueIdentifier getLatestEntryIdForCategory(AtomCategory category) {
		try {
			return atomEntryDAO.getLatestEntryIdForCategory(category);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private List<AtomEntry> getEntriesForFeed(AtomFeed atomFeed) {
		List<AtomEntry> atomEntries = atomEntryDAO.getAtomEntriesForFeed(atomFeed.getId());
		for (AtomEntry atomEntry : atomEntries) {
			atomEntry.setAtomCategories(atomCategoryDAO.getCategoriesForAtomEntry(atomEntry.getAtomEntryId()));
		}
		return atomEntries;
	}
}
