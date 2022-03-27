package com.osiris.vislib.comp;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

@Tag("h2")
public class HMed extends Div {
    public HMed(String txt) {
        setWidthFull();
        setText("  "+txt);
        getStyle().set("color", "#5c5c5c");
        getElement().appendChild(new Element("hr"));
    }
}
