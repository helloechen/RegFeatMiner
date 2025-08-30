package Utility;

import Action.GitAdapter;
import Resource.Resource;

import java.util.ResourceBundle;


public class gitInstance {


    private static GitAdapter ReadInformation(String resourceName) {
        ResourceBundle res = ResourceBundle.getBundle(resourceName);
        String remotePath = res.getString("RemoteGit");
        String localPath = res.getString("LocalGit");
        String branchName = res.getString("branchName");
        String projectName = res.getString("projectName");

        String filePath = localPath + "/" + projectName; 

        return adapter = new GitAdapter(remotePath, filePath, branchName);
    }

    private static GitAdapter adapter = null;

    public static GitAdapter get(String resourceName) {

        return adapter = ReadInformation(resourceName);


    }

    public static GitAdapter get(String projectName, String branchName) {
        //从主文件110行跳转进来，传进来了仓库名字和分支名字（master写死了）
        return adapter = ReadInformation(projectName, branchName);
        //转手把一模一样的任务抛给了本文件的41行，参数抛给ReadInformation函数，向它去要adapter
        //--------+++++++跳转本文件41行++++++++--------------------------


    }

    private static GitAdapter ReadInformation(String projectName, String branchName1) {
        //从本文件34行跳进来，承接了仓库名和分支名（master写死了），仓库名改名叫projectName
//        var remotePath = "https://github.com/apache/" + projectName + ".git";
        var remotePath =Resource.remotePath;
        var localPath = Resource.gitrepository;
        var branchName = Resource.branchName;
        String filePath = localPath + "/" + projectName ;
        //拼接出了远程仓库的路径remotePath:https://github.com/apache/{仓库名}.git
        //还拼接出了一个叫filePath的路径，具体为java_data/仓库名

        return adapter = new GitAdapter(remotePath, filePath, branchName);
    }
}
