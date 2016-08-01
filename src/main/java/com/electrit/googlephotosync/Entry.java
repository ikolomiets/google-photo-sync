package com.electrit.googlephotosync;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Entry {

    private final String id;
    private final String parendId;
    private final String name;
    private final String webViewLink;

    private Folder parentFolder;

    private final static Pattern RE_STARTS_WITH_DIGIT = Pattern.compile("^\\d.*");

    public Entry(String id, String parendId, String name, String webViewLink) {
        if (id == null)
            throw new NullPointerException("id must not be null");

        this.id = id;
        this.parendId = parendId;
        this.name = name;
        this.webViewLink = webViewLink;
    }

    public String getId() {
        return id;
    }

    public String getIdForJava() {
        String result = this.id.replace('-', '_');
        Matcher startsWithDigit = RE_STARTS_WITH_DIGIT.matcher(result);
        if (RE_STARTS_WITH_DIGIT.matcher(result).matches()) {
            result = '_' + result;
        }
        return result;
    }

    public String getJavaPackage(String base) {
        if (parentFolder != null) {
            String result = parentFolder.getJavaPackage(base);
            if (result != null)
                return result + "." + parentFolder.getName();
            else if (parentFolder.getName().equals(base))
                return null;
            else
                return parentFolder.getName();
        } else {
            return null;
        }
    }

    public String getParentId() {
        return parendId;
    }

    public String getName() {
        return name;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry folder = (Entry) o;

        return id.equals(folder.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String ident) {
        return ident + "Entry{" +
                "id='" + id + '\'' +
                "idForJava='" + getIdForJava() + '\'' +
                ", parendId='" + parendId + '\'' +
                ", name='" + name + '\'' +
                ", webViewLink='" + webViewLink + '\'' +
                '}';
    }
}
