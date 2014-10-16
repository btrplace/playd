package org.btrplace.playd.model;

/**
 * @author Fabien Hermenier
 */
public class UseCase {

    private String title;

    private String description;

    private String instance;

    public UseCase(String ti, String desc, String ins) {
        title = ti;
        description = description;
        instance = ins;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String instance() {
        return instance;
    }
}
