package uk.ac.ic.wlgitbridge.bridge.db.sqlite;

import uk.ac.ic.wlgitbridge.bridge.db.DBInitException;
import uk.ac.ic.wlgitbridge.bridge.db.DBStore;
import uk.ac.ic.wlgitbridge.data.model.db.sql.query.GetLatestVersionForProjectSQLQuery;
import uk.ac.ic.wlgitbridge.data.model.db.sql.query.GetPathForURLInProjectSQLQuery;
import uk.ac.ic.wlgitbridge.data.model.db.sql.query.GetProjectNamesSQLQuery;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.create.CreateIndexURLIndexStore;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.create.CreateProjectsTableSQLUpdate;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.create.CreateURLIndexStoreSQLUpdate;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.delete.DeleteFilesForProjectSQLUpdate;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.insert.AddURLIndexSQLUpdate;
import uk.ac.ic.wlgitbridge.data.model.db.sql.update.insert.SetProjectSQLUpdate;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Winston on 17/11/14.
 */
public class SqliteDBStore implements DBStore {

    private final Connection connection;

    public SqliteDBStore(File dbFile) {
        try {
            connection = openConnectionTo(dbFile);
            createTables();
        } catch (Throwable t) {
            throw new DBInitException(t);
        }
    }

    @Override
    public List<String> getProjectNames() {
        return query(new GetProjectNamesSQLQuery());
    }

    @Override
    public void setLatestVersionForProject(
            String projectName,
            int versionID
    ) {
        update(new SetProjectSQLUpdate(projectName, versionID));
    }

    @Override
    public int getLatestVersionForProject(
            String projectName
    ) {
        return query(new GetLatestVersionForProjectSQLQuery(projectName));
    }

    @Override
    public void addURLIndexForProject(
            String projectName,
            String url,
            String path
    ) {
        update(new AddURLIndexSQLUpdate(projectName, url, path));
    }

    @Override
    public void deleteFilesForProject(
            String projectName,
            String... paths
    ) {
        update(new DeleteFilesForProjectSQLUpdate(projectName, paths));
    }

    @Override
    public String getPathForURLInProject(
            String projectName,
            String url
    ) {
        return query(new GetPathForURLInProjectSQLQuery(projectName, url));
    }

    @Override
    public String getOldestUnswappedProject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastAccessedTime(
            String projectName,
            Timestamp time
    ) {
        throw new UnsupportedOperationException();
    }

    private Connection openConnectionTo(File dbFile) {
        File parentDir = dbFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new DBInitException(
                    parentDir.getAbsolutePath() + " directory didn't exist, " +
                            "and unable to create. Check your permissions."
            );
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new DBInitException(e);
        }
        try {
            return DriverManager.getConnection(
                    "jdbc:sqlite:" + dbFile.getAbsolutePath()
            );
        } catch (SQLException e) {
            throw new DBInitException("Unable to connect to DB", e);
        }
    }

    private void createTables() {
        Stream.of(
                new CreateProjectsTableSQLUpdate(),
                new CreateURLIndexStoreSQLUpdate(),
                new CreateIndexURLIndexStore()
        ).forEach(this::update);
    }

    private void update(SQLUpdate update) {
        try {
            doUpdate(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T query(SQLQuery<T> query) {
        try {
            return doQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void doUpdate(SQLUpdate update) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(update.getSQL());
            update.addParametersToStatement(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            statement.close();
        }
    }

    private <T> T doQuery(SQLQuery<T> query) throws SQLException {
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            statement = connection.prepareStatement(query.getSQL());
            query.addParametersToStatement(statement);
            results = statement.executeQuery();
            return query.processResultSet(results);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (results != null) {
                results.close();
            }
        }
    }

}