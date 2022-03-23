package com.osiris.vislib;

import com.osiris.dyml.Dyml;
import com.osiris.dyml.SmartString;
import com.osiris.dyml.exceptions.YamlWriterException;
import com.osiris.events.Event;
import com.vaadin.flow.component.UI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class Config {
    private static final Dyml dyml;
    public static AtomicInteger lastProjectId = new AtomicInteger(0);
    public static Event<List<Project>> onProjectListChange = new Event<List<Project>>().initCleaner(20000, ui -> ((UI) ui).isClosing(), ex -> {
        throw new RuntimeException(ex);
    });
    public static Event<Project> onProjectChange = new Event<Project>().initCleaner(20000, ui -> ((UI) ui).isClosing(), ex -> {
        throw new RuntimeException(ex);
    });

    static {
        try{
            File f = new File(System.getProperty("user.dir")+"/config.dyml");
            f.getParentFile().mkdirs();
            f.createNewFile();
            dyml = new Dyml(f);
            Dyml projects = dyml.put("projects");
            dyml.saveToFile();
            if (projects.children.size() > 1)
                lastProjectId.set(projects.children.get(projects.children.size()-1).get("id").value.asInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveProject(Project project) throws YamlWriterException, IOException {
        boolean notInFile = false;
        synchronized (dyml){
            Dyml savedProject = dyml.get("projects", "" + project.id);
            if(savedProject == null){
                notInFile = true;
                savedProject = dyml.put("projects", "" + project.id);
            }
            savedProject.put("path").value = new SmartString(project.path);
            if (project.name == null) project.name = new File(project.path).getName();
            savedProject.put("name").value = new SmartString(project.name);
            dyml.saveToFile();
            if(project.id > lastProjectId.get()) lastProjectId.set(project.id);
            onProjectChange.execute(project);
        }
        if(notInFile){
            onProjectListChange.execute(getProjects());
        }
    }

    public static void removeProject(Project project) throws YamlWriterException, IOException {
        synchronized (dyml){
            dyml.remove("projects", "" + project.id);
            dyml.saveToFile();
        }
        onProjectListChange.execute(getProjects());
    }

    public static Project getProject(int id){
        for (Project p : getProjects()) {
            if(p.id == id) return p;
        }
        return null;
    }

    public static Project getProject(String path){
        for (Project p : getProjects()) {
            if(p.path.equals(path)) return p;
        }
        return null;
    }

    public static List<Project> getProjects(){
        List<Project> projects = new ArrayList<>();
        synchronized (dyml){
            Dyml savedProject = dyml.put("projects");
            savedProject.debugPrint(System.out);
            for (Dyml p :
                    savedProject.getChildren()) {
                projects.add(new Project(p.get("id").asInt(), p.get("path").asString(), p.get("name").asString(),
                        new ArrayList<>())); // TODO
            }
        }
        return projects;
    }

}
