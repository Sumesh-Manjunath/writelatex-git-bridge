package uk.ac.ic.wlgitbridge.writelatex.api.request.getsavedvers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.ic.wlgitbridge.writelatex.api.request.base.Request;
import uk.ac.ic.wlgitbridge.writelatex.api.request.base.Result;
import uk.ac.ic.wlgitbridge.writelatex.api.request.exception.FailedConnectionException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Winston on 06/11/14.
 */
public class SnapshotGetSavedVersResult extends Result {

    private List<SnapshotInfo> savedVers;

    public SnapshotGetSavedVersResult(Request request, JsonElement json) throws FailedConnectionException {
        super(request, json);
    }

    public SnapshotGetSavedVersResult(List<SnapshotInfo> savedVers) {
        this.savedVers = savedVers;
    }

    @Override
    public JsonElement toJson() {
        JsonArray jsonThis = new JsonArray();
        for (SnapshotInfo savedVer : savedVers) {
            JsonObject jsonSavedVer = new JsonObject();
            jsonSavedVer.addProperty("versionId", savedVer.getVersionId());
            jsonSavedVer.addProperty("comment", savedVer.getComment());
            WLUser user = savedVer.getUser();
            JsonObject jsonUser = new JsonObject();
            jsonUser.addProperty("email", user.getEmail());
            jsonUser.addProperty("name", user.getName());
            jsonSavedVer.add("user", jsonUser);
            jsonSavedVer.addProperty("createdAt", savedVer.getCreatedAt());
            jsonThis.add(jsonSavedVer);
        }
        return jsonThis;
    }

    @Override
    public void fromJSON(JsonElement json) {
        savedVers = new LinkedList<SnapshotInfo>();
        for (JsonElement elem : json.getAsJsonArray()) {
            savedVers.add(new Gson().fromJson(elem.getAsJsonObject(), SnapshotInfo.class));
        }
    }

    public List<SnapshotInfo> getSavedVers() {
        return savedVers;
    }

}
