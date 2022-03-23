package com.osiris.vislib;

import java.util.List;

public class Project {
    public int id;
    public String path;
    public String name;
    public List<Lib> libraries;

    public Project(int id, String path, String name, List<Lib> libraries) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.libraries = libraries;
    }
}
