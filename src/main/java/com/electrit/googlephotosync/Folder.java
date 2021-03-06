package com.electrit.googlephotosync;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Folder extends Entry {

    private final Set<Entry> children = new HashSet<>();

    public Folder(String id, String parendId, String name, String webViewLink, String createdTime) {
        super(id, parendId, name, webViewLink, createdTime);
    }

    public Set<Entry> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public void addChild(Entry entry) {
        if (!entry.getParentId().equals(getId()))
            throw new IllegalArgumentException("Folder's id doesn't match entry's parentId");
        children.add(entry);
    }

    public String toString(String ident) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString(ident));
        for (Entry child : children) {
            sb.append("\n").append(child.toString('\t' + ident));
        }

        return sb.toString();
    }
}
