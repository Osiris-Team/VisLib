package com.osiris.vislib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.osiris.vislib.comp.HMin;
import com.osiris.vislib.comp.VLScroll;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Route("projects")
public class ProjectView extends VerticalLayout implements HasUrlParameter<Integer> {
    public HorizontalLayout hl = new HorizontalLayout();
    public Accordion accordion = new Accordion();
    public File selectedFolder;

    public ProjectView() {
        add(hl, accordion);
        Config.onProjectListChange.addAction(list -> {
            accordion.getElement().removeAllChildren();
            for (Project p : list) {
                accordion.add(p.name, getSingleProjectView(p));
            }
        }, ex -> {
            throw new RuntimeException(ex);
        }, false, UI.getCurrent());
        for (Project p : Config.getProjects()) {
            System.err.println(p.id+p.path);
            accordion.add(p.name, getSingleProjectView(p));
        }
        hl.setWidthFull();
        hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        accordion.setWidthFull();

        Button addProject = new Button("Add existing project");
        addProject.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addProject.setWidthFull();
        addProject.addClickListener(click -> {
            VerticalLayout vl = new VerticalLayout();
            File startFolder;
            List<Project> projectList = Config.getProjects();
            if (projectList.size() > 0)
                startFolder = new File(projectList.get(projectList.size() - 1).path.trim());
            else startFolder = new File(System.getProperty("user.dir"));

            VerticalLayout vlFiles = new VerticalLayout();
            vl.add(vlFiles);
            vlFiles.setSpacing(false);
            vlFiles.setPadding(false);
            vlFiles.setHeight("600px");
            vlFiles.setWidth("500px");
            vlFiles.getStyle().set("overflow", "auto");
            addFolders(startFolder, vlFiles);

            Button save = new Button("Save");
            vl.add(save);
            save.setWidthFull();
            save.addClickListener(click2 -> {
                try {
                    Config.saveProject(new Project(Config.lastProjectId.incrementAndGet(), selectedFolder.toString(), selectedFolder.getName(),
                            new ArrayList<>())); // TODO
                    new Notification("Added new project!");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            new Dialog(vl).open();
        });
        hl.add(addProject);
    }

    private VerticalLayout addFolders(File folder, VerticalLayout vl) {
        vl.removeAll();
        if (folder.getParentFile() != null)
            vl.add(createFileButton("...", folder.getParentFile(), () -> selectedFolder = folder.getParentFile(), vl));
        System.err.println(folder.getAbsolutePath());
        for (File f :
                folder.listFiles()) {
            vl.add(createFileButton(f.getName(), f, () -> selectedFolder = f, vl));
        }
        return vl;
    }

    private Button createFileButton(String fName, File f, Runnable runnable, VerticalLayout vl) {
        Button btn;
        if (f.isDirectory()) {
            btn = new Button(fName, new Icon(VaadinIcon.FOLDER));
            btn.addClickListener(click -> {
                addFolders(f, vl);
                runnable.run();
                new Notification("Updated selection.").open();
            });
        } else {
            btn = new Button(fName, new Icon(VaadinIcon.FILE));
            btn.setEnabled(false);
            btn.addClickListener(click -> {
                new Notification("Only folders can be selected.").open();
            });
        }
        btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        return btn;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Integer pID) {
        try {
            Project p = Config.getProject(pID);
            if (p == null) {
                add(new Span("No project saved by id " + pID + "."));
                return;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private VerticalLayout getSingleProjectView(Project p) {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.add(new Span("Project ID: " + p.id));
        vl.add(new Span("Project path: " + p.path));
        vl.add(new Span("Libs:"));
        for (Lib lib :
                p.libraries) {
            vl.add(new Span(lib.name));
        }
        Button addLib = new Button("+lib");
        addLib.addClickListener(click -> {
            createAddLibDialog(p.path).open();
        });
        vl.add(addLib);
        return vl;
    }

    private Dialog createAddLibDialog(String projectPath) {
        VLScroll vl = new VLScroll();
        vl.setPadding(false);
        vl.setHeight("100%");
        VerticalLayout vlResults = new VerticalLayout();
        vlResults.setPadding(false);
        HorizontalLayout hlSearch = new HorizontalLayout();
        vl.add(hlSearch);
        hlSearch.setWidthFull();
        hlSearch.setPadding(false);
        TextField tfSearch = new TextField();
        hlSearch.add(tfSearch);
        tfSearch.setWidthFull();
        tfSearch.setPlaceholder("Search by library name...");
        Button btnSearch = new Button(new Icon(VaadinIcon.SEARCH));
        hlSearch.add(btnSearch);
        btnSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSearch.addClickShortcut(Key.ENTER);
        btnSearch.addClickListener(click -> {
            try {
                JsonObject obj =
                        U.getGithubJsonElement("https://api.github.com/search/repositories?q=" + tfSearch.getValue() + "+language:c&sort=stars&order=desc").getAsJsonObject();
                vlResults.add(buildRepoCards(obj.get("items").getAsJsonArray()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        vl.add(vlResults);

        vl.add(new HMin("Updated last 24h"));
        VerticalLayout vlFresh = new VerticalLayout();
        vl.add(vlFresh);
        vlFresh.setPadding(false);
        vlFresh.add(buildRepoCards(Github.freshRepos));
        vl.add(getRecommendedLibs());
        return new Dialog(vl);
    }

    private Component[] buildRepoCards(JsonArray repos) {
        HorizontalLayout[] lys = new HorizontalLayout[repos.size()];
        for (int i = 0; i < repos.size(); i++) {
            JsonObject repo = repos.get(i).getAsJsonObject();
            String fullName = repo.get("full_name").getAsString();
            String starsCount = repo.get("stargazers_count").getAsString();
            String desc = "";
            if(repo.get("description") != null && !repo.get("description").isJsonNull())
                desc = repo.get("description").getAsString();
            HorizontalLayout hl = new HorizontalLayout();
            hl.setWidthFull();
            hl.setPadding(false);
            hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            lys[i] = hl;
            VerticalLayout vlDetails = new VerticalLayout();
            hl.add(vlDetails);
            vlDetails.getStyle().set("overflow", "hidden");
            vlDetails.setPadding(false);
            vlDetails.setSpacing(false);
            Button btnName = new Button(fullName);
            vlDetails.add(btnName);
            btnName.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnName.addClickListener(click -> {
                getElement().executeJs("window.open('https://github.com/" + fullName + "', '_blank').focus();");
            });
            btnName.getStyle().set("cursor", "pointer");
            //VerticalLayout vlSpace = new VerticalLayout();
            //hl.add(vlSpace);
            Button stars = new Button(starsCount + "   " + desc, new Icon(VaadinIcon.STAR));
            vlDetails.add(stars);
            stars.setEnabled(false);
            stars.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            Button btnAdd = new Button("+");
            hl.add(btnAdd);
            btnAdd.getStyle().set("cursor", "pointer");
            btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            vlDetails.setWidth("90%");
            btnAdd.setWidth("10%");
            //TODO
        }
        return lys;
    }

    private VerticalLayout getRecommendedLibs() {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);

        return vl;
    }
}
