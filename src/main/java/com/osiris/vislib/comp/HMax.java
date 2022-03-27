package com.osiris.vislib.comp;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

@Tag("h1")
public class HMax extends Div {
    public HMax(String txt) {
        setWidthFull();
        setText("  "+txt);
        getStyle().set("color", "#525252");
        getElement().appendChild(new Element("hr"));
    }
}
