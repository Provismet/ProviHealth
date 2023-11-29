package com.provismet.provihealth.config;

import java.util.Iterator;

/**
 * One day I'll just write my own library, but until then I will just copy+paste this one class into my mods as needed lmao.
 */
public class JsonHelper {
    private StringBuilder builder;
    private int indentationLevel;

    public JsonHelper () {
        this.builder = new StringBuilder();
        this.indentationLevel = 0;
    }

    public JsonHelper start () {
        this.builder.append("{\n");
        this.indentationLevel++;
        return this;
    }

    public JsonHelper append (String value) {
        this.indent();
        this.builder.append("\"").append(value).append("\"");
        return this;
    }

    public JsonHelper append (String key, String value) {
        this.indent();
        this.builder.append("\"").append(key).append("\": ");
        this.builder.append("\"").append(value).append("\"");
        return this;
    }

    public JsonHelper append (String key, int value) {
        this.indent();
        this.builder.append("\"").append(key).append("\": ").append(value);
        return this;
    }

    public JsonHelper append (String key, boolean value) {
        this.indent();
        this.builder.append("\"").append(key).append("\": ").append(value);
        return this;
    }

    public JsonHelper append (String key, float value) {
        this.indent();
        this.builder.append("\"").append(key).append("\": ").append(value);
        return this;
    }

    public JsonHelper append (String key, double value) {
        this.indent();
        this.builder.append("\"").append(key).append("\": ").append(value);
        return this;
    }

    public JsonHelper closeObject () {
        this.indentationLevel--;
        this.indent();
        this.builder.append("}");
        return this;
    }

    public JsonHelper startArray (String key) {
        this.indent();
        this.builder.append("\"").append(key).append("\": [");
        this.indentationLevel++;
        return this;
    }

    public JsonHelper closeArray () {
        this.indentationLevel--;
        this.indent();
        this.builder.append("]");
        return this;
    }

    public JsonHelper createArray (String key, Iterable<String> values) {
        this.startArray(key).newLine(false);

        Iterator<String> iter = values.iterator();

        if (iter.hasNext()) {
            String last = iter.next();
            
            while (iter.hasNext()) {
                this.append(last).newLine(true);
                last = iter.next();
            }

            this.append(last);
        }

        this.newLine(false);
        this.closeArray();
        return this;
    }

    public JsonHelper createArray (String key, String... values) {
        return this.createArray(key, values);
    }

    /**
     * Creates a new line with a comma.
     * @return this
     */
    public JsonHelper newLine () {
        return this.newLine(true);
    }

    public JsonHelper newLine (boolean addComma) {
        if (addComma) this.builder.append(",");
        this.builder.append("\n");
        return this;
    }

    private void indent () {
        for (int i = 0; i < this.indentationLevel; ++i) {
            this.builder.append("\t");
        }
    }

    @Override
    public String toString () {
        return this.builder.toString();
    }
}
