package Utility;

import Action.CodeDiff;
import Action.GitAdapter;
import Date.DateAction;
import Obj.CommitMessage;
import Regrex.RegrexDefinations;
import Resource.Resource;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class MatchUtility {


    public static CommitMessage findCorresponding(GitAdapter adapter,String file_path, List<CommitMessage> copyOfRange,boolean isIgnore) throws Exception {

        var Production_name=RegrexDefinations.testTransformProduct(file_path); 

        for(CommitMessage commitMessage:copyOfRange){
            if(commitMessage.getCommitId()==null||commitMessage.getLastCommitId()==null){
                continue;
            }
            Object[] diff_result = CodeDiff.diffCommitToCommit(adapter, commitMessage.getCommitId(), commitMessage.getLastCommitId()
            , isIgnore);
            List<DiffEntry> diffs=(List<DiffEntry>)diff_result[1];
            DiffFormatter formatter=(DiffFormatter)diff_result[0];
            var diff_fileNames=new ArrayList<String>();
            for(DiffEntry diffEntry:diffs){
                if(formatter.toFileHeader(diffEntry).toEditList().size()!=0){
                    if(diffEntry.getChangeType()== DiffEntry.ChangeType.DELETE){
                        diff_fileNames.add(diffEntry.getOldPath());
                    }else {
                        diff_fileNames.add(diffEntry.getNewPath());
                    }
                }
            }
            for(String fileName:diff_fileNames){
                if(fileName.equals(Production_name)){
                    return commitMessage;
                }
            }
        }
        Production_name=Production_name.toUpperCase();


        for(CommitMessage commitMessage:copyOfRange){

            if(commitMessage.getCommitId()==null||commitMessage.getLastCommitId()==null){
                continue;
            }
            Object diff_result[]=CodeDiff.diffCommitToCommit(adapter, commitMessage.getCommitId(), commitMessage.getLastCommitId()
                    , isIgnore);
            List<DiffEntry> diffs=(List<DiffEntry>)diff_result[1];
            DiffFormatter formatter=(DiffFormatter)diff_result[0];
            var diff_fileNames=new ArrayList<String>();
            for(DiffEntry diffEntry:diffs){
                if(formatter.toFileHeader(diffEntry).toEditList().size()!=0){
                    if(diffEntry.getChangeType()== DiffEntry.ChangeType.DELETE){
                        diff_fileNames.add(diffEntry.getOldPath().toUpperCase());
                    }else {
                        diff_fileNames.add(diffEntry.getNewPath().toUpperCase());
                    }
                }
            }
            for(String fileName:diff_fileNames){
                if(fileName.equals(Production_name.toUpperCase())){
                    return commitMessage;
                }
            }
        }
        return null;
    }



    public static List<CommitMessage> getCommits_range(int index,CommitMessage[] all){

        CommitMessage nowCommit=all[index];  
        List<CommitMessage> result=new ArrayList<>();
        for(int i=index;i<all.length;i++){
            if(isInRange(nowCommit,all[i])){
                result.add(all[i]);
            }
        }
        return result;
    }

    private static boolean isInRange(CommitMessage now, CommitMessage previous){
        ResourceBundle resourceBundle=ResourceBundle.getBundle("parameter");
        String PositiveTime=resourceBundle.getString("PositiveTime");
        return DateAction.get_diff(now.getCommitDate(), previous.getCommitDate()) <= Double.parseDouble(PositiveTime);

    }

    public static List<RevCommit> getCommits_range(GitAdapter adapter,CommitMessage index){
        try {
            ResourceBundle resourceBundle=ResourceBundle.getBundle("parameter");
            String PositiveTime=resourceBundle.getString("PositiveTime").trim();
            var endDate= new Date(DateAction.ConvertTDate(index.getCommitDate()).getTime()-Long.parseLong(PositiveTime)*1000);
            var files=adapter.getRevLog(index.getCommitId(), endDate);
            return files;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getFilesName(GitAdapter adapter, List<CommitMessage> commitMessages) {
        List<String> files_name = null;
        try {
            files_name = ProcedureUtility.getAllFiles(adapter, commitMessages, adapter.getProjectName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files_name;
    }


    public static List<CommitMessage>  getCommitMessages(GitAdapter adapter) {

        var commitMessages=new ArrayList<CommitMessage>();
        File commitDirectory=new File(Resource.commitInfo);

        if(!commitDirectory.exists()){
            commitDirectory.mkdir();
        }

        File Commits_File = new File(commitDirectory.getPath()+File.separator+ adapter.getProjectName() + ".csv");
        System.out.println(Commits_File.getPath());
        //拼接出./Commits/{仓库名}.csv路径名字，叫Commits_File，并打印出来
        //如果该仓库是第一次被提取commitmessage,则./Commits/{仓库名}.csv还不存在，会进入到else部分。否则会进入到if部分里面

        if (Commits_File.exists()) {
            commitMessages = (ArrayList<CommitMessage>) ProcedureUtility.getAllCommits(Commits_File);
            //说明已经提取过并写入过此项目的commit信息
            //利用getAllCommits方法，传入csv文件的路径，把之前写进去的commit信息反序列化后再读出来

        } else {
            try {
                commitMessages = (ArrayList<CommitMessage>) adapter.getNo_MergeCommitMessages();
                //利用adapter的getNo_MergeCommitMessages获取commitmessage,返回写入commitMessages
                //-------+++跳转到/Action/Gitadapter313行+++++---------------------------------
                //回来看，adapter的getNo_MergeCommitMessages获取了此项目每次非merge提交的信息，是个commitmessage的列表

                ProcedureUtility.WriteCommits(commitMessages, Commits_File);
                //利用WriteCommits，传入此项目commitMessages的提交信息列表，写入Commits_File路径：./Commits/{仓库名}.csv
                //-------+++跳转到Utility/Procedure 132行++++ ----------------------------------

            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        }
        return commitMessages;
    }
}
