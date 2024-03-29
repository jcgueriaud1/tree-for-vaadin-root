package org.vaadin.jchristophe.bean;

/**
 * @author jcgueriaud
 */
public class DummyFile {

    private int id;
    private String code;
    private String filename;
    private String icon;
    private DummyFile parent;
    private int level;

    public DummyFile(int id, String code, String filename, String icon, DummyFile parent) {
        this.id = id;
        this.code = code;
        this.filename = filename;
        this.icon = icon;
        this.parent = parent;
        if (parent == null) {
            level = 0;
        } else {
            level = parent.getLevel() + 1;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public DummyFile getParent() {
        return parent;
    }

    public void setParent(DummyFile parent) {
        this.parent = parent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return code + " - " + filename;
    }
}
