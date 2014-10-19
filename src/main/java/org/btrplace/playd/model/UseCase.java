package org.btrplace.playd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import javax.persistence.Id;

/**
 * @author Fabien Hermenier
 */
public class UseCase {

    @Id
    private String key;

    private String title;

    private String description;

    private String model;

    private String script;

    private int hits;

    private long lastHit;

    public UseCase() {

    }

    @ObjectId
    @JsonProperty("_id")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public UseCase(String k, String ti, String desc, String mo, String scr) {
        key = k;
        title = ti;
        description = desc;
        model = mo;
        script = scr;
        hits = 0;
        lastHit = System.currentTimeMillis();
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public void hit() {
        hits++;
        lastHit = System.currentTimeMillis();
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public long getLastHit() {
        return lastHit;
    }

    public void setLastHit(long lastHit) {
        this.lastHit = lastHit;
    }

    public String summary() {
        StringBuilder res = new StringBuilder();
        res.append("{");
        res.append("\"key\":\"").append(key()).append("\",");
        res.append("\"title\":\"").append(title()).append("\"");
        res.append("}");
        return res.toString();
    }
    public String key() {
        return key;
    }

    public String model() {
        return model;
    }

    public String script() {
        return script;
    }

    @Override
    public String toString() {
        return "UseCase{" +
                "key='" + key + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
