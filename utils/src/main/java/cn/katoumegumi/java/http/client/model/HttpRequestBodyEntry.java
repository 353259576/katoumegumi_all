package cn.katoumegumi.java.http.client.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class HttpRequestBodyEntry implements Comparable<HttpRequestBodyEntry> {
    private String name;
    private String value;
    //private File fileValue;
    private Object objectValue;
    private InputStream inputStreamValue;
    private boolean isFile = false;

    public HttpRequestBodyEntry() {

    }

    public HttpRequestBodyEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public HttpRequestBodyEntry(String name, File file) {
        try {
            this.name = name;
            this.value = file.getName();
            this.inputStreamValue = new FileInputStream(file);
            this.isFile = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpRequestBodyEntry(String name, String fileName, InputStream inputStream) {
        this.name = name;
        this.value = fileName;
        this.inputStreamValue = inputStream;
        this.isFile = true;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public InputStream getInputStreamValue() {
        return inputStreamValue;
    }

    public void setInputStreamValue(InputStream inputStreamValue) {
        this.inputStreamValue = inputStreamValue;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(Object objectValue) {
        this.objectValue = objectValue;
    }


    @Override
    public int compareTo(HttpRequestBodyEntry o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpRequestBodyEntry) {
            if (((HttpRequestBodyEntry) obj).getName().equals(this.name)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
