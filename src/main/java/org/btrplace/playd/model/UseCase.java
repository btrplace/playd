package org.btrplace.playd.model;

/**
 * @author Fabien Hermenier
 */
public class UseCase {

    private String key;

    private String title;

    private String description;

    private String model;

    private String script;

    public UseCase(String k, String ti, String desc, String mo, String scr) {
        key = k;
        title = ti;
        description = desc;
        model = mo;
        script = scr;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String toJson() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append("\"key\": \"").append(key()).append("\",");
        b.append("\"title\": \"").append(title()).append("\",");
        b.append("\"description\": \"").append(description()).append("\",");
        b.append("\"model\": \"").append(model()).append("\",");
        b.append("\"script\": \"").append(script()).append("\"");
        return b.append("}").toString();
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
}
