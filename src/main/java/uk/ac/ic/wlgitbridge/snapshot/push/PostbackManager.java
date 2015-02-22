package uk.ac.ic.wlgitbridge.snapshot.push;

import uk.ac.ic.wlgitbridge.snapshot.push.exception.InvalidPostbackKeyException;
import uk.ac.ic.wlgitbridge.snapshot.push.exception.SnapshotPostException;
import uk.ac.ic.wlgitbridge.snapshot.push.exception.UnexpectedPostbackException;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Winston on 17/11/14.
 */
public class PostbackManager {

    private final SecureRandom random;
    private final Map<String, PostbackContents> postbackContentsTable;

    public PostbackManager() {
        random = new SecureRandom();
        postbackContentsTable = new HashMap<String, PostbackContents>();
    }

    public int getVersionID(String projectName) throws SnapshotPostException {
        try {
            return postbackContentsTable.get(projectName).waitForPostback();
        } catch (SnapshotPostException e) {
            throw e;
        } finally {
            postbackContentsTable.remove(projectName);
        }
    }

    public void postVersionIDForProject(String projectName, int versionID, String postbackKey) throws UnexpectedPostbackException {
        getPostbackForProject(projectName).receivedVersionID(versionID, postbackKey);
    }

    public void postExceptionForProject(String projectName, SnapshotPostException exception, String postbackKey) throws UnexpectedPostbackException {
        getPostbackForProject(projectName).receivedException(exception, postbackKey);
    }

    private PostbackContents getPostbackForProject(String projectName) throws UnexpectedPostbackException {
        PostbackContents contents = postbackContentsTable.remove(projectName);
        if (contents == null) {
            throw new UnexpectedPostbackException();
        }
        return contents;
    }

    public String makeKeyForProject(String projectName) {
        String key = System.currentTimeMillis() + randomString();
        PostbackContents contents = new PostbackContents(key);
        postbackContentsTable.put(projectName, contents);
        return key;
    }

    public void checkPostbackKey(String projectName, String postbackKey) throws InvalidPostbackKeyException {
        postbackContentsTable.get(projectName).checkPostbackKey(postbackKey);
    }

    private String randomString() {
        return new BigInteger(130, random).toString(32);
    }

}