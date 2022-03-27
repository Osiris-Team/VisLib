package com.osiris.vislib;

import com.osiris.dyml.Dyml;
import com.osiris.dyml.exceptions.YamlWriterException;
import com.osiris.events.Event;
import com.vaadin.flow.component.UI;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class Config {
    private static final Dyml dyml;
    public static AtomicInteger lastProjectId = new AtomicInteger(0);
    public static Event<List<Project>> onProjectListChange = new Event<List<Project>>().initCleaner(20000, ui -> ((UI) ui)!=null && ((UI) ui).isClosing(), ex -> {
        throw new RuntimeException(ex);
    });
    public static Event<Project> onProjectChange = new Event<Project>().initCleaner(20000, ui -> ((UI) ui)!=null && ((UI) ui).isClosing(), ex -> {
        throw new RuntimeException(ex);
    });

    static {
        try {
            System.out.println("[DEBUG "+new Date()+"] "+new Throwable().getStackTrace()[0].getMethodName());
            File f = new File(System.getProperty("user.dir") + "/config.dyml");
            f.getParentFile().mkdirs();
            f.createNewFile();

            dyml = new Dyml(f);
            Dyml projects = dyml.put("projects");
            if (projects.children.size() > 1)
                lastProjectId.set(projects.lastChild().get("id").value.asInt());
            Dyml freshRepos = dyml.put("last-fetch-fresh-repos");
            if (freshRepos.asString() == null)
                freshRepos.value.set(Instant.now().minus(Duration.ofDays(2)).toEpochMilli());

            dyml.saveToFile();
            System.out.println();
            dyml.debugPrint(System.out);
            onProjectChange.addAction(val -> {
                dyml.load(f);
            }, ex -> {
                throw new RuntimeException(ex);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveProject(Project project) throws YamlWriterException, IOException {
        System.out.println("[DEBUG "+new Date()+"] "+new Throwable().getStackTrace()[0].getMethodName());
        boolean notInFile = false;
        Dyml savedProject = dyml.get("projects", "" + project.id);
        if (savedProject == null) {
            notInFile = true;
            savedProject = dyml.put("projects", "" + project.id);
        }
        Objects.requireNonNull(project.path);
        project.path = project.path.replaceAll("\\\\", "/");
        savedProject.put("path").value.set(project.path);
        if (project.name == null) project.name = new File(project.path).getName();
        savedProject.put("name").value.set(project.name);
        dyml.saveToFile();
        if (project.id > lastProjectId.get()) lastProjectId.set(project.id);
        onProjectChange.execute(project);
        if (notInFile) {
            onProjectListChange.execute(getProjects());
        }
    }

    public static void removeProject(Project project) throws YamlWriterException, IOException {
        System.out.println("[DEBUG "+new Date()+"] "+new Throwable().getStackTrace()[0].getMethodName());
        dyml.remove("projects", "" + project.id);
        dyml.saveToFile();
        onProjectListChange.execute(getProjects());
    }

    public static Project getProject(int id) {
        for (Project p : getProjects()) {
            if (p.id == id) return p;
        }
        return null;
    }

    public static Project getProject(String path) {
        for (Project p : getProjects()) {
            if (p.path.equals(path)) return p;
        }
        return null;
    }

    public static List<Project> getProjects() {
        System.out.println("[DEBUG "+new Date()+"] "+new Throwable().getStackTrace()[0].getMethodName());
        List<Project> list = new ArrayList<>();
        System.out.println();
        dyml.debugPrint(System.out);
        Dyml projects = dyml.put("projects");
        for (Dyml p :
                projects.getChildren()) {
            list.add(new Project(Integer.parseInt(p.key), p.get("path").asString(), p.get("name").asString(),
                    new ArrayList<>())); // TODO
        }
        return list;
    }

    public static long getLastFetchFreshReposTime() {
        return dyml.get("last-fetch-fresh-repos").asLong();
    }

    public static void setLastFetchFreshReposTime(long currentTimeMillis) throws YamlWriterException, IOException {
        synchronized (dyml) {
            dyml.get("last-fetch-fresh-repos").value.set(currentTimeMillis);
            dyml.saveToFile();
        }
    }
}
