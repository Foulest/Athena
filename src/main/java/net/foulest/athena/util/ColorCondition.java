package net.foulest.athena.util;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class ColorCondition {

    public boolean applicable;
    public List<Attribute> attributes;

    public ColorCondition(boolean applicable, Attribute... attributes) {
        this.applicable = applicable;
        this.attributes = Arrays.asList(attributes);
    }

    public String colorize(String text) {
        return Ansi.colorize(text, attributes.toArray(new Attribute[0]));
    }
}
