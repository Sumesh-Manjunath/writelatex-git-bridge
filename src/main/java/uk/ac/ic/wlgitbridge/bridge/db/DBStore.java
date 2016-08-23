package uk.ac.ic.wlgitbridge.bridge.db;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by winston on 20/08/2016.
 */
public interface DBStore {

    List<String> getProjectNames();

    void setLatestVersionForProject(String project, int versionID);

    int getLatestVersionForProject(String project);

    void addURLIndexForProject(String projectName, String url, String path);

    void deleteFilesForProject(String project, String... files);

    String getPathForURLInProject(String projectName, String url);

    String getOldestUnswappedProject();

    /**
     * Sets the last accessed time for the given project name.
     * @param projectName the project's name
     * @param time the time, or null if the project is to be swapped
     */
    void setLastAccessedTime(String projectName, Timestamp time);

}