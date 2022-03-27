package com.osiris.vislib.comp;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

@Tag("h3")
public class HMin extends Div {
    public HMin(String txt) {
        setWidthFull();
        setText("  "+txt);
        getStyle().set("color", "#7e7e7e");
        getElement().appendChild(new Element("hr"));
    }
}
