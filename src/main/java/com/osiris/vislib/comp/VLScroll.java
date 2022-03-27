package com.osiris.vislib.comp;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@CssImport("./styles/scrollbar.css")
public class VLScroll extends VerticalLayout {
    public VLScroll() {
        setHeight("500px");
        getStyle().set("overflow", "auto");
    }
}
