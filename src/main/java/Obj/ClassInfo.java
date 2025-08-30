package Obj;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Objects;

public class ClassInfo {
    private String prod_path;
    private String test_path;
    private String prod_time;
    private String test_time;
    private String pro_commitID;
    private String test_commitID;
    private String project;
    private String classFile;
    private String className;
    private List<Integer> addLines;
    private List<Integer> delLines;
    private String isfound;

    public String getProd_path() {
        return prod_path;
    }

    public void setProd_path(String prod_path) {
        this.prod_path = prod_path;
    }

    public String getTest_path() {
        return test_path;
    }

    public void setTest_path(String test_path) {
        this.test_path = test_path;
    }


    public String getProd_time() {
        return prod_time;
    }

    public void setProd_time(String prod_time) {
        this.prod_time = prod_time;
    }

    public String getTest_time() {
        return test_time;
    }

    public void setTest_time(String test_time) {
        this.test_time = test_time;
    }

    public String getPro_commitID() {
        return pro_commitID;
    }

    public void setPro_commitID(String pro_commitID) {
        this.pro_commitID = pro_commitID;
    }

    public String getTest_commitID() {
        return test_commitID;
    }

    public void setTest_commitID(String test_commitID) {
        this.test_commitID = test_commitID;
    }

    public String getIsfound() {
        return isfound;
    }

    public void setIsfound(String isfound) {
        this.isfound = isfound;
    }



    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getClassFile() {
        return classFile;
    }

    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Integer> getAddLines() {
        return addLines;
    }

    public void setAddLines(List<Integer> addLines) {
        this.addLines = addLines;
    }

    public List<Integer> getDelLines() {
        return delLines;
    }

    public void setDelLines(List<Integer> delLines) {
        this.delLines = delLines;
    }
}