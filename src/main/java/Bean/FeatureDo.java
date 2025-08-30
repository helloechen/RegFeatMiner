package Bean;
//FeatureDo类是用于记录代码commit变更特征的自建类

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FeatureDo {

    @JsonIgnore
    String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    String repository;
    String prod_path;
    String test_path;
    String prod_time;
    String test_time;
    String type;
    String proType;

    int add_annotation_line=0;
    int add_call_line=0;
    int add_classname_line=0;
    int add_condition_line=0;
    int add_field_line=0;
    int add_import_line=0;
    int add_packageid_line=0;
    int add_parameter_line=0;
    int add_return_line=0;
    int del_annotation_line=0;
    int del_call_line=0;
    int del_classname_line=0;
    int del_condition_line=0;
    int del_field_line=0;
    int del_import_line=0;
    int del_packageid_line=0;
    int del_parameter_line=0;
    int del_return_line=0;
//    int changed_tokens=0;
    String label;
    String prod_commitID;
    String test_commitID;
    String isfound;
    String originPro;
    String changedPro;
    String originTest;
    String changedTest;
    String commitMessage;
    String test_commitMessage;

    public int triggering_3(){
        int flag = 0;
        if(add_annotation_line == 0 && add_call_line == 0 && add_classname_line == 0
                && add_condition_line == 0 && add_field_line == 0 && add_packageid_line == 0 && add_parameter_line == 0
                && add_return_line == 0 && del_annotation_line == 0 && del_call_line == 0 && del_classname_line == 0
                && del_condition_line == 0 && del_field_line == 0 && del_packageid_line == 0 && del_parameter_line == 0
                && del_return_line == 0){
            if(add_import_line != 0 && del_import_line != 0){
                flag = 3;
            }
            else if(add_import_line != 0){
                flag = 1;
            }
            else if(del_import_line != 0){
                flag = 2;
            }
        }
        return flag;
    }

    public Boolean isAllZero(){
        Boolean flag = false;
        if(add_annotation_line == 0 && add_call_line == 0 && add_classname_line == 0
                && add_condition_line == 0 && add_field_line == 0 && add_packageid_line == 0 && add_parameter_line == 0
                && add_return_line == 0 && del_annotation_line == 0 && del_call_line == 0 && del_classname_line == 0
                && del_condition_line == 0 && del_field_line == 0 && del_packageid_line == 0 && del_parameter_line == 0
                && del_return_line == 0 && add_import_line == 0 && del_import_line == 0){
            flag = true;
        }
        return flag;
    }

    public String getProd_commitID() {
        return prod_commitID;
    }

    public void setProd_commitID(String prod_CommitID) {
        this.prod_commitID = prod_CommitID;
    }

    public String getProType() {
        return proType;
    }

    public void setProType(String proType) {
        this.proType = proType;
    }

    public String getTest_commitID() {
        return test_commitID;
    }

    public void setTest_commitID(String test_CommitID) {
        this.test_commitID = test_CommitID;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAdd_annotation_line() {
        return add_annotation_line;
    }

    public void setAdd_annotation_line(int add_annotation_line) {
        this.add_annotation_line = add_annotation_line;
    }

    public int getAdd_call_line() {
        return add_call_line;
    }

    public void setAdd_call_line(int add_call_line) {
        this.add_call_line = add_call_line;
    }

    public int getAdd_classname_line() {
        return add_classname_line;
    }

    public void setAdd_classname_line(int add_classname_line) {
        this.add_classname_line = add_classname_line;
    }

    public int getAdd_condition_line() {
        return add_condition_line;
    }

    public void setAdd_condition_line(int add_condition_line) {
        this.add_condition_line = add_condition_line;
    }

    public int getAdd_field_line() {
        return add_field_line;
    }

    public void setAdd_field_line(int add_field_line) {
        this.add_field_line = add_field_line;
    }

    public int getAdd_import_line() {
        return add_import_line;
    }

    public void setAdd_import_line(int add_import_line) {
        this.add_import_line = add_import_line;
    }

    public int getAdd_packageid_line() {
        return add_packageid_line;
    }

    public void setAdd_packageid_line(int add_packageid_line) {
        this.add_packageid_line = add_packageid_line;
    }

    public int getAdd_parameter_line() {
        return add_parameter_line;
    }

    public void setAdd_parameter_line(int add_parameter_line) {
        this.add_parameter_line = add_parameter_line;
    }

    public int getAdd_return_line() {
        return add_return_line;
    }

    public void setAdd_return_line(int add_return_line) {
        this.add_return_line = add_return_line;
    }

    public int getDel_annotation_line() {
        return del_annotation_line;
    }

    public void setDel_annotation_line(int del_annotation_line) {
        this.del_annotation_line = del_annotation_line;
    }

    public int getDel_call_line() {
        return del_call_line;
    }

    public void setDel_call_line(int del_call_line) {
        this.del_call_line = del_call_line;
    }

    public int getDel_classname_line() {
        return del_classname_line;
    }

    public void setDel_classname_line(int del_classname_line) {
        this.del_classname_line = del_classname_line;
    }

    public int getDel_condition_line() {
        return del_condition_line;
    }

    public void setDel_condition_line(int del_condition_line) {
        this.del_condition_line = del_condition_line;
    }

    public int getDel_field_line() {
        return del_field_line;
    }

    public void setDel_field_line(int del_field_line) {
        this.del_field_line = del_field_line;
    }

    public int getDel_import_line() {
        return del_import_line;
    }

    public void setDel_import_line(int del_import_line) {
        this.del_import_line = del_import_line;
    }

    public int getDel_packageid_line() {
        return del_packageid_line;
    }

    public void setDel_packageid_line(int del_packageid_line) {
        this.del_packageid_line = del_packageid_line;
    }

    public int getDel_parameter_line() {
        return del_parameter_line;
    }

    public void setDel_parameter_line(int del_parameter_line) {
        this.del_parameter_line = del_parameter_line;
    }

    public int getDel_return_line() {
        return del_return_line;
    }

    public void setDel_return_line(int del_return_line) {
        this.del_return_line = del_return_line;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public String getIsfound() {
        return isfound;
    }

    public void setIsfound(String isfound) {
        this.isfound = isfound;
    }

    public String getOriginPro() {
        return originPro;
    }

    public void setOriginPro(String originPro) {
        this.originPro = originPro;
    }

    public String getChangedPro() {
        return changedPro;
    }

    public void setChangedPro(String changedPro) {
        this.changedPro = changedPro;
    }
    public String getOriginTest() {
        return originTest;
    }

    public void setOriginTest(String originTest) {
        this.originTest = originTest;
    }

    public String getChangedTest() {
        return changedTest;
    }

    public void setChangedTest(String changedTest) {
        this.changedTest = changedTest;
    }

//    public int getChanged_tokens() {
//        return changed_tokens;
//    }
//
//    public void setChanged_tokens(int changed_tokens) {
//        this.changed_tokens = changed_tokens;
//    }


    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getTest_commitMessage() {
        return test_commitMessage;
    }

    public void setTest_commitMessage(String test_commitMessage) {
        this.test_commitMessage = test_commitMessage;
    }



    @Override
    public String toString() {
        return "FeatureDo{" +
                "repository='" + repository + '\'' +
                ", prod_path='" + prod_path + '\'' +
                ", test_path='" + test_path + '\'' +
                ", prod_time='" + prod_time + '\'' +
                ", test_time='" + test_time + '\'' +
                ", test_type='" + type + '\'' +
                ", add_annotation_line=" + add_annotation_line +
                ", add_call_line=" + add_call_line +
                ", add_classname_line=" + add_classname_line +
                ", add_condition_line=" + add_condition_line +
                ", add_field_line=" + add_field_line +
                ", add_import_line=" + add_import_line +
                ", add_packageid_line=" + add_packageid_line +
                ", add_parameter_line=" + add_parameter_line +
                ", add_return_line=" + add_return_line +
                ", del_annotation_line=" + del_annotation_line +
                ", del_call_line=" + del_call_line +
                ", del_classname_line=" + del_classname_line +
                ", del_condition_line=" + del_condition_line +
                ", del_field_line=" + del_field_line +
                ", del_import_line=" + del_import_line +
                ", del_packageid_line=" + del_packageid_line +
                ", del_parameter_line=" + del_parameter_line +
                ", del_return_line=" + del_return_line +
                ", label='" + label + '\'' +
                ", pro_Commit='" + prod_commitID + '\'' +
                ", test_Commit='" + test_commitID + '\'' +
                '}';
    }

    @JsonIgnore
    public boolean isEmpty() {
        if((add_annotation_line==0)&&
                (add_call_line==0)&&
        add_classname_line==0&&
        add_condition_line==0&&
        add_field_line==0&&
        add_import_line==0&&
        add_packageid_line==0&&
        add_parameter_line==0&&
        add_return_line==0&&
        del_annotation_line==0&&
        del_call_line==0&&
        del_classname_line==0&&
        del_condition_line==0&&
        del_field_line==0&&
        del_import_line==0&&
        del_packageid_line==0&&
        del_parameter_line==0&&
        del_return_line==0){
            return true;
        }
        return false;
    }
}