package org.vaadin.jchristophe.service;

import org.vaadin.jchristophe.bean.DummyFile;

import java.util.ArrayList;
import java.util.List;

public class DummyFileService {

    public static String iconFolder = "vaadin:folder-open-o";
    public static String iconFile = "vaadin:file-o";

    private int maximumLevel = 4;
    private int maximumElementPerLevel = 5;

    private int nextId = 1;

    public DummyFileService() {

    }
    public DummyFileService(int maximumLevel, int maximumElementPerLevel) {
        this.maximumLevel = maximumLevel;
        this.maximumElementPerLevel = maximumElementPerLevel;
    }

    public int getChildCount(DummyFile dummyFile) {
        return hasChildren(dummyFile)?maximumElementPerLevel:0;
    }

    public Boolean hasChildren(DummyFile dummyFile) {
        if (dummyFile == null) return true;
        return (dummyFile.getLevel() < maximumLevel);
    }

    public List<DummyFile> fetchRoot() {
       return createDummyFileList(null, 10);
    }

    public List<DummyFile> fetchAllChildren(DummyFile parent) {
        return fetchChildren(parent, 10);
    }
    public List<DummyFile> fetchChildren(DummyFile parent, int offset) {
        if ( hasChildren(parent)) {
            return createDummyFileList(parent, offset);
        } else {
            return new ArrayList<>();
        }
    }

    private List<DummyFile> createDummyFileList(DummyFile parent, int offset) {
        List<DummyFile> dummyFileList = new ArrayList<>();
        for (int i = offset; i < maximumElementPerLevel + offset; i++) {
            String code = (parent != null)? parent.getCode() +  "." + i: ""+i;
            String icon = (parent == null)? iconFolder : ((parent.getLevel() + 1  == maximumLevel)?iconFile:iconFolder);
            DummyFile file = new DummyFile(nextId, code, "Item " + code, icon, parent);
            nextId++;
            dummyFileList.add(file);
        }
        return dummyFileList;
    }

    public void setMaximumLevel(int maximumLevel) {
        this.maximumLevel = maximumLevel;
    }

    public void setMaximumElementPerLevel(int maximumElementPerLevel) {
        this.maximumElementPerLevel = maximumElementPerLevel;
    }
}