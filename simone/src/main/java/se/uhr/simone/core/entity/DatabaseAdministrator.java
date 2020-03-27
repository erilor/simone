package se.uhr.simone.core.entity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import se.uhr.simone.extension.api.entity.DatabaseAdmin;

@ApplicationScoped
public class DatabaseAdministrator implements DatabaseAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseAdministrator.class);

	@Inject
	@FeedDS
	DataSource ds;

	/*
	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		Flyway flyway = Flyway.configure().dataSource(ds).load();
		flyway.migrate();
	
	}
	*/

	@Override
	public void dropTables() {
		LOG.info("delete all tables");
		SqlScriptRunner runner = new SqlScriptRunner(new JdbcTemplate(ds));
		runner.execute(this.getClass().getResourceAsStream("/db/delete_all_tables.sql"));
	}

}
