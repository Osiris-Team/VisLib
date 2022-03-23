package com.osiris.vislib;

import com.vaadin.flow.component.Html;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import java.io.File;
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
        }, ex -> {throw new RuntimeException(ex);}, false, UI.getCurrent());
        for (Project p : Config.getProjects()) {
            accordion.add(p.name, getSingleProjectView(p));
        }
        hl.setWidthFull();
        hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        accordion.setWidthFull();

        Html title = new Html("<h3>Projects</h3>");
        Button addProject = new Button("+");
        addProject.addClickListener(click -> {
            VerticalLayout vl = new VerticalLayout();
            File startFolder;
            List<Project> projectList = Config.getProjects();
            if(projectList.size() > 0)
                startFolder = new File(projectList.get(projectList.size()-1).path);
            else
                startFolder = new File(System.getProperty("user.dir"));
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
                try{
                    Config.saveProject(new Project(Config.lastProjectId.incrementAndGet(), selectedFolder.getName(), selectedFolder.toString(),
                            new ArrayList<>())); // TODO
                    new Notification("Added new project!");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            new Dialog(vl).open();
        });
        hl.add(title, addProject);
    }

    private VerticalLayout addFolders(File folder, VerticalLayout vl){
        vl.removeAll();
        vl.add(createFileButton("...", folder.getParentFile(), () -> selectedFolder = folder.getParentFile(), vl));
        for (File f :
                folder.listFiles()) {
            vl.add(createFileButton(f.getName(), f, () -> selectedFolder = f, vl));
        }
        return vl;
    }

    private Button createFileButton(String fName, File f, Runnable runnable, VerticalLayout vl) {
        Button btn;
        if(f.isDirectory()){
            btn = new Button(fName, new Icon(VaadinIcon.FOLDER));
            btn.addClickListener(click -> {
                addFolders(f, vl);
                runnable.run();
                new Notification("Updated selection.").open();
            });
        } else{
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
        try{
            Project p = Config.getProject(pID);
            if(p == null){
                add(new Span("No project saved by id "+pID+"."));
                return;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private VerticalLayout getSingleProjectView(Project p){
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.add(new Span("Project ID: "+p.id));
        vl.add(new Span("Project path: "+p.path));
        return vl;
    }
}
