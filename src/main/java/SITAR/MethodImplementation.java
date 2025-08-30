package SITAR;

import Action.CodeDiff;
import Action.GitAdapter;
import Bean.FeatureDo;
import Date.DateAction;
import IntervalTreeInstance.IntervalTree;
import Obj.ClassInfo;
import Obj.CommitMessage;
import PareCode.PareInstance;
import PareCode.SpecificTreeParser;
import Persistent.Serialization;
import Resource.Resource;
import Utility.gitInstance;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import SITAR.CodeCleaner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.PrintStream;
import java.util.regex.Pattern;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import static Utility.MatchUtility.getCommitMessages;

public class MethodImplementation{

    public static void main(String[] args){
        String projectName = Resource.projectName;
        File filelog = new File(projectName+".log");

        try{
            PrintStream fileOut = new PrintStream(filelog);
            System.setOut(fileOut);
        }catch (IOException e){
            e.printStackTrace();
        }

        File totalDir =new File(Resource.inputDir);
        File[] categoriesDir = totalDir.listFiles();

        for(int i=0;i<categoriesDir.length;i++){
            System.out.println(categoriesDir[i].getName());
        }

        File FeatureDir=new File(Resource.outputDir);
        assert categoriesDir != null;
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        for(File file:categoriesDir) {
            File dir=new File(FeatureDir+File.separator+file.getName()+File.separator);
            System.out.println(file.getName());

            if(dir.exists()){
                System.out.println(dir.getAbsolutePath());
               System.out.println("please ensure that ./Feature directory does not has this project name!");
               System.exit(1);
            }

            try {
                processCategory(file);

//                Collections.sort(result, new Comparator<FeatureDo>() {
//                    @Override
//                    public int compare(FeatureDo o1, FeatureDo o2) {
//                        if(DateAction.get_diff(o1.getProd_time(),o2.getProd_time())>0){
//                            return 1;
//                        }else  if(DateAction.get_diff(o1.getProd_time(),o2.getProd_time())==0){
//                            return 0;
//                        }else{
//                            return -1;
//                        }
//                    }
//                });

//                dir=new File(FeatureDir+File.separator+projectName+File.separator);
//                if(!dir.exists()){
//                    FileUtils.forceMkdir(dir);
//                }
//
//                for(int i=0;i<result.size();i++){
//                    try(BufferedWriter writer=new BufferedWriter(
//                            new FileWriter(dir.getPath()+File.separator+(i+1)+".json")
//                    )){
//                        writer.write(Serialization.ObjToJSON(result.get(i)));
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processCategory(File file_dir) throws IOException {
//        boolean fflag = false;
        File FeatureDir=new File(Resource.outputDir);
        File dir=new File(FeatureDir+File.separator+file_dir.getName()+File.separator);
        int countNumber = 0;
        if(!dir.exists()){
            FileUtils.forceMkdir(dir);
        }
        var filenames= FileUtils.listFiles(file_dir,null,true);
        String reposity=file_dir.getName();
        GitAdapter adapter = gitInstance.get(reposity, "main");

        System.out.println("this");
        System.out.println(reposity);
        System.out.println("this");
        adapter.initGit();
        List<CommitMessage> commitMessages = getCommitMessages(adapter);

        //为策略1先做个缓存铺垫
        Map<Integer, List<String>> changedFilesMap = new HashMap<>();
        int length = commitMessages.size();
        for (int i = length - 2; i > 0; i--) {
//        for (int i = length - 2; i > length/2; i--) {
            try {
                var CommitMessage = commitMessages.get(i);
                var CommitMessageID = CommitMessage.getCommitId();
                var ParentmessageID = commitMessages.get(i).getLastCommitId();
                List<String> changedFiles = adapter.findChangedFile(CommitMessageID, ParentmessageID);
                changedFilesMap.put(i, changedFiles);
            }catch (Exception e){
                System.out.println("Exception found!");
            }
        }

//        var result=new ArrayList<FeatureDo>();
        CommitMessage finalCommitMessage=commitMessages.get(0);

        int tri_num_strategy1 = 0;
        int num_strategy1 = 0;
        int tri_num_strategy2 = 0;
        int num_strategy2 = 0;
        int tri_num_strategy3 = 0;
        int num_strategy3 = 0;
        int tri_num_strategy4 = 0;
        int num_strategy4 = 0;
        int tri_num_strategy5 = 0;
        int num_strategy5 = 0;
        int tri_num_strategy6 = 0;
        int num_strategy6 = 0;
        int tri_num_strategy7 = 0;
        int num_strategy7 = 0;
        int all_zero = 0;

        Repository localrepository = null;
        GitHistoryRefactoringMinerImpl miner = null;

        if(Resource.fineTune){
            try{
                FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();

                String repoPath = "./java_data/" + reposity;
                localrepository = repositoryBuilder.setGitDir(new File(repoPath + "/.git"))
                        .readEnvironment()
                        .findGitDir()
                        .build();
                miner = new GitHistoryRefactoringMinerImpl();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        for(File singleFile:filenames) {
            System.out.println("-----------------------------------\n");
            System.out.println(singleFile.getName());
            var classInfo = Serialization.json2Bean(readContents(singleFile), ClassInfo.class);
            Boolean isfound = false;
            if("found test change".equals(classInfo.getIsfound())){
                isfound = true;
            }

            FeatureDo featureDo = new FeatureDo();
            featureDo.setRepository(reposity);
            featureDo.setProd_path(classInfo.getProd_path());
            featureDo.setTest_path(classInfo.getTest_path());
            featureDo.setTest_time(classInfo.getTest_time());
            featureDo.setProd_time(classInfo.getProd_time());
            featureDo.setProd_commitID(classInfo.getPro_commitID());
            featureDo.setTest_commitID(classInfo.getTest_commitID());
            featureDo.setIsfound(classInfo.getIsfound());
            featureDo.setType("");
            featureDo.setLabel("");

            if(isfound) {
                double Hours_between;
                if (featureDo.getTest_time() == null || featureDo.getTest_time().equals("")) {
                    Hours_between = new BigInteger(String.valueOf(DateAction.get_diff(finalCommitMessage.getCommitDate(), featureDo.getProd_time()))).divide(new BigInteger(String.valueOf(60)))
                            .divide(new BigInteger(String.valueOf(60))).doubleValue();

                } else {
                    Hours_between = new BigInteger(String.valueOf(DateAction.get_diff(featureDo.getTest_time(), featureDo.getProd_time()))).divide(new BigInteger(String.valueOf(60)))
                            .divide(new BigInteger(String.valueOf(60))).doubleValue();
                }

                if (Hours_between <= 12 && featureDo.getTest_time() != null) {
                    featureDo.setLabel("POSITIVE");
                    System.out.println("initially judged as POSITIVE");
                    //打上正样本标签

                } else if (Hours_between > 12 && featureDo.getTest_time() != null) {
                    featureDo.setLabel("NEGATIVE");
                    System.out.println("initially judged as NEGATIVE");
                    //打上负样本标签
                } else {
                    featureDo.setLabel("UNDEFINE");
                    continue;
                }
            }else {
                featureDo.setLabel("NEGATIVE");
            }

            if (!isfound){
                try {
                    int index = getIndex(featureDo.getProd_commitID(), commitMessages);
                    var proCommitMessage = commitMessages.get(index);
                    var diffentry = adapter.getDiffOfFileInCommit(proCommitMessage, featureDo.getProd_path());
                    String testContent = adapter.getCommitSpecificFileContent(proCommitMessage.getLastCommitId(),featureDo.getTest_path());
                    featureDo.setOriginTest(testContent);
                    featureDo.setChangedTest("");
                    featureDo.setCommitMessage(proCommitMessage.getCommitMessage());
                    featureDo.setTest_commitMessage("");

                    if (diffentry.getChangeType() == DiffEntry.ChangeType.ADD) {
                        String newContent = adapter.getCommitSpecificFileContent(proCommitMessage.getCommitId(), featureDo.getProd_path());
                        var newIntervalTree = PareInstance.ParseFile(newContent);
                        featureDo.setProType("CREATE");
                        featureDo.setOriginPro("");
                        featureDo.setChangedPro(newContent);
                        fillFeatures(featureDo, newIntervalTree, "ADD");
//                        featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount("", newContent));
                    } else if (diffentry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                        String oldContent = adapter.getCommitSpecificFileContent(proCommitMessage.getLastCommitId(), featureDo.getProd_path());
                        var oldIntervalTree = PareInstance.ParseFile(oldContent);
                        featureDo.setProType("DELETE");
                        featureDo.setChangedPro("");
                        featureDo.setOriginPro(oldContent);
                        fillFeatures(featureDo, oldIntervalTree, "DEL");
//                        featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount(oldContent, ""));
                    } else {
                        if (diffentry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                            featureDo.setProType("EDIT");
                        }else if(diffentry.getChangeType() == DiffEntry.ChangeType.COPY){
                            featureDo.setProType("COPY");
                        }else if(diffentry.getChangeType() == DiffEntry.ChangeType.RENAME){
                            featureDo.setProType("RENAME");
                        }
                        String newContent = adapter.getCommitSpecificFileContent(proCommitMessage.getCommitId(), diffentry.getNewPath());
                        String oldContent = adapter.getCommitSpecificFileContent(proCommitMessage.getLastCommitId(), diffentry.getOldPath());

                        featureDo.setOriginPro(oldContent);
                        featureDo.setChangedPro(newContent);

                        var newIntervalTree = PareInstance.ParseFile(newContent);
                        var oldIntervalTree = PareInstance.ParseFile(oldContent);
                        var prodiff = CodeDiff.prepareDiffMethod(adapter, proCommitMessage.getCommitId(),
                                proCommitMessage.getLastCommitId(), CodeDiff.getDiffFormatter(true, null, adapter.getRepository()), diffentry, true);
                        var addFeature = getModifyFeatures(newIntervalTree, prodiff, "ADD");
                        var delFeature = getModifyFeatures(oldIntervalTree, prodiff, "DEL");
                        fillModifyFeatures(featureDo, addFeature, "ADD");
                        fillModifyFeatures(featureDo, delFeature, "DEL");
//                        featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount(oldContent, newContent));
                    }
                    System.out.println(Serialization.ObjToJSON(featureDo));
                    if(featureDo.isAllZero()){
                        all_zero++;
                        System.out.println("ALL ZERO " + featureDo.getLabel() +" "+ featureDo.getType());
//                        continue;
                    }
//                    result.add(featureDo);
                    try(BufferedWriter writer=new BufferedWriter(
                            new FileWriter(dir.getPath()+File.separator+(countNumber+1)+".json")
                    )){
                        countNumber++;
                        writer.write(Serialization.ObjToJSON(featureDo));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    continue;
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("Exception happen!");
                    continue;
                }
            }

            int index=getIndex(featureDo.getProd_commitID(),commitMessages);
            System.out.println(index);
            int index2=getIndex(featureDo.getTest_commitID(),commitMessages);
            System.out.println(index2);
            boolean exempt = false;
            try{
                var proCommitMessage=commitMessages.get(index);
                var TESTCommitMessage=commitMessages.get(index2);
                System.out.println(proCommitMessage);
                System.out.println(TESTCommitMessage);
                featureDo.setCommitMessage(proCommitMessage.getCommitMessage());
                featureDo.setTest_commitMessage(TESTCommitMessage.getCommitMessage());

                var diffentry=adapter.getDiffOfFileInCommit(proCommitMessage, featureDo.getProd_path());
                System.out.println(diffentry);
                var diffentry2=adapter.getDiffOfFileInCommit(TESTCommitMessage, featureDo.getTest_path());
                System.out.println(diffentry2);

                if (diffentry2.getChangeType() == DiffEntry.ChangeType.ADD) {
                    String newContent_ = adapter.getCommitSpecificFileContent(TESTCommitMessage.getCommitId(), diffentry2.getNewPath());
                    featureDo.setType("CREATE");
                    featureDo.setChangedTest(newContent_);
                    featureDo.setOriginTest("");
                }
                else if (diffentry2.getChangeType() == DiffEntry.ChangeType.DELETE) {
                    String oldContent_ = adapter.getCommitSpecificFileContent(TESTCommitMessage.getLastCommitId(), diffentry2.getOldPath());
                    featureDo.setType("DELETE");
                    featureDo.setOriginTest(oldContent_);
                    featureDo.setChangedTest("");
                    System.out.println("DEREFIX!");
                }
                else if (diffentry2.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                    featureDo.setType("EDIT");
                }
                if (diffentry2.getChangeType() == DiffEntry.ChangeType.COPY) {
                    featureDo.setType("COPY");
                }
                if (diffentry2.getChangeType() == DiffEntry.ChangeType.RENAME) {
                    featureDo.setType("RENAME");
                }
                String newContent_ = adapter.getCommitSpecificFileContent(TESTCommitMessage.getCommitId(), diffentry2.getNewPath());
                String oldContent_ = adapter.getCommitSpecificFileContent(TESTCommitMessage.getLastCommitId(), diffentry2.getOldPath());
                featureDo.setOriginTest(oldContent_);
                featureDo.setChangedTest(newContent_);

                if(index == index2 ){
                    exempt = true;
                }

                if(Resource.fineTune){
                    //微调策略1
                    if(diffentry.getChangeType()!= DiffEntry.ChangeType.MODIFY ||diffentry2.getChangeType()!= DiffEntry.ChangeType.MODIFY){
                        if("NEGATIVE".equals(featureDo.getLabel())){
                            tri_num_strategy1++;
                            if(index == index2 || index == index2 + 1){
                                featureDo.setLabel("POSITIVE");
                                System.out.println("fine tune as POSITIVE by strategy 1");
                                num_strategy1++;
                            }else {
                                Boolean Flag = false;
                                for(int k = index - 1; k > index2 ; k--){
                                    List<String> changefile = changedFilesMap.get(k);
                                    for(String changeFilename : changefile ){
                                        System.out.println(changeFilename);
                                        if(changeFilename.contains(".java")){
                                            System.out.println("not trigger");
                                            Flag = true;
                                            break;
                                        }
                                    }
                                    if(Flag){
                                        break;
                                    }
                                }
                                if(!Flag){
                                    featureDo.setLabel("POSITIVE");
                                    System.out.println("fine tune as POSITIVE by strategy 1");
                                    num_strategy1++;
                                    exempt = true;
                                }
                            }
                        }
                    }
//
                    //微调策略2
                    if(((index - index2) >= 3) && "POSITIVE".equals(featureDo.getLabel())){
                        System.out.println("triggering strategy 2");
                        tri_num_strategy2++;
                        Boolean flag = false;
                        for(int i = index - 1; i > index2; i--){
                            List<String> changedFiles = changedFilesMap.get(i);
                            for (String file : changedFiles){
                                if(!file.toLowerCase().contains("test")){
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        var Hours_between = new BigInteger(String.valueOf(DateAction.get_diff(featureDo.getTest_time(), featureDo.getProd_time()))).divide(new BigInteger(String.valueOf(60)))
                                .divide(new BigInteger(String.valueOf(60))).doubleValue();
                        if(Hours_between <= 1){
                            flag = false;
                            System.out.println("实证研究2");
                            exempt = true;
                        }
                        if(flag){
                            num_strategy2++;
                            System.out.println("fine tune as NEGATIVE by strategy 2");
                            featureDo.setLabel("NEGATIVE");
                        }
                    }
                }

                if(diffentry.getChangeType()== DiffEntry.ChangeType.ADD){
                    String newContent=adapter.getCommitSpecificFileContent(proCommitMessage.getCommitId(),  featureDo.getProd_path());
                    var newIntervalTree= PareInstance.ParseFile(newContent);
                    featureDo.setProType("CREATE");
                    featureDo.setChangedPro(newContent);
                    featureDo.setOriginPro("");
                    fillFeatures(featureDo,newIntervalTree,"ADD");
//                    featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount("", newContent));

                }else if(diffentry.getChangeType()==DiffEntry.ChangeType.DELETE){
                    String oldContent=adapter.getCommitSpecificFileContent(proCommitMessage.getLastCommitId(),  featureDo.getProd_path());
                    var oldIntervalTree= PareInstance.ParseFile(oldContent);
                    featureDo.setProType("DELETE");
                    featureDo.setOriginPro(oldContent);
                    featureDo.setChangedPro("");
                    fillFeatures(featureDo,oldIntervalTree,"DEL");
//                    featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount(oldContent, ""));

                }else{
                    if (diffentry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                        featureDo.setProType("EDIT");
                    }else if(diffentry.getChangeType() == DiffEntry.ChangeType.COPY){
                        featureDo.setProType("COPY");
                    }else if(diffentry.getChangeType() == DiffEntry.ChangeType.RENAME){
                        featureDo.setProType("RENAME");
                    }

                    String newContent=adapter.getCommitSpecificFileContent(proCommitMessage.getCommitId(), diffentry.getNewPath());
                    String oldContent=adapter.getCommitSpecificFileContent(proCommitMessage.getLastCommitId(), diffentry.getOldPath());

                    featureDo.setOriginPro(oldContent);
                    featureDo.setChangedPro(newContent);

                    var newIntervalTree= PareInstance.ParseFile(newContent);
                    var oldIntervalTree= PareInstance.ParseFile(oldContent);
                    var prodiff= CodeDiff.prepareDiffMethod(adapter,proCommitMessage.getCommitId(),
                            proCommitMessage.getLastCommitId(),CodeDiff.getDiffFormatter(true,null,adapter.getRepository()),diffentry,true);
//                    System.out.println(6);
                    var addFeature=getModifyFeatures(newIntervalTree,prodiff,"ADD");
                    var delFeature=getModifyFeatures(oldIntervalTree,prodiff,"DEL");
                    fillModifyFeatures(featureDo,addFeature,"ADD");
                    fillModifyFeatures(featureDo,delFeature,"DEL");
//                    featureDo.setChanged_tokens(SITAR.CodeCleaner.getModifiedUnitsCount(oldContent,newContent));
                }
            }
            catch(NullPointerException e){
                System.out.println("NullPointerException caught, go on!");
                continue;
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Exception caught, go on!");
                continue;
            }

            if(Resource.fineTune){
                //微调策略3,6
                try{
                    if (featureDo.triggering_3()!= 0 && !exempt){
                        if("POSITIVE".equals(featureDo.getLabel())){
                            System.out.println("triggering strategy 3");
                            tri_num_strategy3++;
                        }else if("NEGATIVE".equals(featureDo.getLabel())){
                            System.out.println("triggering strategy 6");
                            tri_num_strategy6++;
                        }

                        System.out.println(featureDo.getAdd_import_line());
                        System.out.println(featureDo.getDel_import_line());
                        String newContent = featureDo.getChangedPro();
                        String oldContent = featureDo.getOriginPro();

                        String[] importLines1 = extractImportLines(newContent);
                        String[] importLines2 = extractImportLines(oldContent);
                        String[] changedimport = findDifferences(importLines1,importLines2);
                        System.out.println("production import changed as follows:");
                        for (String line : changedimport) {
                            System.out.println(line);
                        }

                        String Test_newContent = featureDo.getChangedTest();
                        String Test_oldContent = featureDo.getOriginTest();

                        String[] Test_importLines1 = extractImportLines(Test_newContent);
                        String[] Test_importLines2 = extractImportLines(Test_oldContent);
                        String[] Test_changedimport = findDifferences(Test_importLines1,Test_importLines2);
                        System.out.println("test import changed as follows:");
                        for (String line : Test_changedimport) {
                            System.out.println(line);
                        }
                        if(!hasIntersection(changedimport,Test_changedimport) && "POSITIVE".equals(featureDo.getLabel())){
                            if(changedimport.length != 0){
                                System.out.println("fine tune as NEGATIVE by strategy 3");
                                featureDo.setLabel("NEGATIVE");
                                num_strategy3++;
                            }
                        }else{
                            if(hasIntersection(changedimport,Test_changedimport) && "POSITIVE".equals(featureDo.getLabel())){
                                exempt = true;
                            }
                            if(changedimport.length != 0 && Test_changedimport.length != 0 && hasIntersection(changedimport,Test_changedimport) && "NEGATIVE".equals(featureDo.getLabel())) {
                                System.out.println("fine tune as POSITIVE by strategy 6");
                                featureDo.setLabel("POSITIVE");
                                num_strategy6++;
                            }
                        }
                    }
                }catch(Exception e){
                    System.out.println("Exception caught in strategy3,6, go on!");
                }

                //微调策略4,7
                try{
                    if("POSITIVE".equals(featureDo.getLabel()) && !exempt){
                        Boolean skip=false;

                        String newContent = featureDo.getChangedPro();
                        String TestnewContent = featureDo.getChangedTest();
                        String TestoldContent = featureDo.getOriginTest();
                        String oldContent = featureDo.getOriginPro();

                        if(newContent.isEmpty() || TestoldContent.isEmpty() || TestnewContent.isEmpty() || oldContent.isEmpty()){
                            skip=true;
                        }
                        if(!skip){
                            System.out.println("triggering strategy 4");
                            tri_num_strategy4++;

                            List<String> changedUnit1 = SITAR.CodeCleaner.getModifiedUnits(oldContent,newContent);
                            List<String> changedUnit2 = SITAR.CodeCleaner.getModifiedUnits(TestoldContent,TestnewContent);
                            Set<String> changedUnitSet1 = new HashSet<>(changedUnit1);
                            Set<String> changedUnitSet2 = new HashSet<>(changedUnit2);
                            if(changedUnitSet1.size() != 0 && changedUnitSet2.size() != 0){
                                changedUnitSet1.retainAll(changedUnitSet2);
                                if(changedUnitSet1.size() == 0){
                                    num_strategy4++;
                                    System.out.println("fine tune as NEGATIVE by strategy 4");
                                    featureDo.setLabel("NEGATIVE");
                                }else if(changedUnitSet1.size() >= 3){
                                    exempt = true;
                                }
                            }
                        }
                    }else if("NEGATIVE".equals(featureDo.getLabel())){
                        Boolean skip=false;
                        String newContent = featureDo.getChangedPro();
                        String TestnewContent = featureDo.getChangedTest();
                        String TestoldContent = featureDo.getOriginTest();
                        String oldContent = featureDo.getOriginPro();
                        if(newContent.isEmpty() || TestoldContent.isEmpty() || TestnewContent.isEmpty() || oldContent.isEmpty()){
                            skip=true;
                        }
                        if(!skip){
                            System.out.println("triggering strategy 7");
                            tri_num_strategy7++;

                            List<String> changedUnit1 = SITAR.CodeCleaner.getModifiedUnits(oldContent,newContent);
                            List<String> changedUnit2 = SITAR.CodeCleaner.getModifiedUnits(TestoldContent,TestnewContent);
                            Set<String> changedUnitSet1 = new HashSet<>(changedUnit1);
                            Set<String> changedUnitSet2 = new HashSet<>(changedUnit2);
                            if(changedUnitSet1.size() != 0 && changedUnitSet2.size() != 0){
                                changedUnitSet1.retainAll(changedUnitSet2);
                                if(changedUnitSet1.size() >= 3){
                                    num_strategy7++;
                                    System.out.println("fine tune as POSITIVE by strategy 7");
                                    featureDo.setLabel("POSITIVE");
                                }
                            }
                        }
                    }
                }catch(Exception e){
                    System.out.println("Exception Type: " + e.getClass().getName());
                    System.out.println(e.getMessage());
                    System.out.println("Exception caught, go on!");
                }

                //微调策略5
                try {
                    if ("EDIT".equals(featureDo.getType()) && "POSITIVE".equals(featureDo.getLabel()) && !exempt) {
                        System.out.println("triggering strategy 5");
                        tri_num_strategy5++;
                        boolean[] fflag= {false};
                        String[] refactorINFO = {"",""};
                        var testCommitMessage = commitMessages.get(index2);
                        var ProCommitMessage = commitMessages.get(index);
                        String commitId = testCommitMessage.getCommitId();
                        String ProcommitId = ProCommitMessage.getCommitId();
                        try {
                            //挖掘重构
                            miner.detectAtCommit(localrepository, commitId, new RefactoringHandler() {
                                @Override
                                public void handle(String commitId, List<Refactoring> refactorings) {
                                    System.out.println("Commit ID: " + commitId);
                                    Boolean found = false;
                                    if (refactorings.isEmpty()) {
                                        System.out.println("No refactorings found in test commit.");
                                    } else {
                                        System.out.println("Detected refactorings in test commit:");
                                        String s1 = featureDo.getTest_path();
                                        int lastIndex = s1.lastIndexOf('/');
                                        String result = s1.substring(lastIndex + 1, s1.length() - 5);
                                        System.out.println(result);
                                        String pattern1 = result + "$";
                                        for (Refactoring ref : refactorings) {
                                            String s = ref.toString();
                                            Pattern pattern = Pattern.compile(".*" + pattern1);
                                            if (pattern.matcher(s).matches()) {
                                                System.out.println(s);
                                                refactorINFO[0] = s;
                                                fflag[0] = true;
                                                System.out.println("found TARGET test refactored in strategy 5");
                                                System.out.println("triggering strategy 5+");
                                                found = true;
                                            }
                                            if (found) {
                                                break;
//                                            fflag = true;
                                            }
                                        }
                                        if(!found){
                                            System.out.println("NOT found TARGET test refactored in strategy 5");
                                        }
                                    }
                                }
                            });
                            if(fflag[0] == true){
                                miner.detectAtCommit(localrepository, ProcommitId, new RefactoringHandler() {
                                    @Override
                                    public void handle(String commitId, List<Refactoring> refactorings) {
                                        System.out.println("seeking for refactors in ProCommit ID: " + commitId);
                                        Boolean found = false;
                                        if (refactorings.isEmpty()) {
                                            System.out.println("No refactorings found in ProCommit ID.");
                                        } else {
                                            System.out.println("Detected refactorings in ProCommit ID:");
                                            String s1 = featureDo.getProd_path();
                                            int lastIndex = s1.lastIndexOf('/');
                                            String result = s1.substring(lastIndex + 1, s1.length() - 5);
                                            System.out.println(result);
                                            String pattern1 = "\\." + result + "$";
                                            for (Refactoring ref : refactorings) {
                                                String s = ref.toString();
                                                Pattern pattern = Pattern.compile(".*" + pattern1);
                                                if (pattern.matcher(s).matches()) {
                                                    System.out.println(s);
                                                    refactorINFO[1] = s;
                                                    System.out.println("found TARGET Pro refactored in strategy 5");
                                                    found = true;
                                                }
                                                if (found) {
                                                    break;
                                                }
                                            }
                                            if(!found){
                                                System.out.println("NOT found TARGET Pro refactored in strategy 5");
                                                System.out.println("fine tune as NEGATIVE by strategy 5");
                                                featureDo.setLabel("NEGATIVE");
                                            }else{
                                                System.out.println("judge futher! and continue");
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception Type: " + e.getClass().getName());
                    System.out.println(e.getMessage());
                    System.out.println("Exception caught, go on!");
                }
            }

            if(featureDo.isAllZero()){
                all_zero++;
                System.out.println("ALL ZERO " + featureDo.getLabel() +" "+ featureDo.getType());
//                continue;
            }

            System.out.println(Serialization.ObjToJSON(featureDo));
//            result.add(featureDo);
            try(BufferedWriter writer=new BufferedWriter(
                    new FileWriter(dir.getPath()+File.separator+(countNumber+1)+".json")
            )){
                countNumber++;
                writer.write(Serialization.ObjToJSON(featureDo));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(tri_num_strategy1);
        System.out.println(num_strategy1);
        System.out.println(tri_num_strategy2);
        System.out.println(num_strategy2);
        System.out.println(tri_num_strategy3);
        System.out.println(num_strategy3);
        System.out.println(tri_num_strategy4);
        System.out.println(num_strategy4);
        System.out.println(tri_num_strategy5);
        System.out.println("num of strategy 5 should be searched by 'fine tune as NEGATIVE by strategy 5'");
        System.out.println(tri_num_strategy6);
        System.out.println(num_strategy6);
        System.out.println(tri_num_strategy7);
        System.out.println(num_strategy7);
        System.out.println(all_zero);
        if(localrepository != null){
            localrepository.close(); // 关闭仓库
        }

        return;
    }


    private static HashMap<SpecificTreeParser.ResultEnum, Integer> getModifyFeatures(HashMap<SpecificTreeParser.ResultEnum, IntervalTree<Integer>> IntervalTree, ClassInfo prodiff, String Category) {
        var result=new HashMap<SpecificTreeParser.ResultEnum, Integer>();
        switch (Category){
            case "ADD":
                var addLines=prodiff.getAddLines();
                for(Integer value:addLines){
                    IntervalTree.forEach((key, value1) -> {
                        result.putIfAbsent(key, 0);
                        if (value1.query(value).size() != 0) {
                            result.put(key, result.get(key) + 1);
                        }
                    });
                }
                break;
            case "DEL":
                var delLines=prodiff.getDelLines();
                for(Integer value:delLines){
                    IntervalTree.forEach((key, value1) -> {
                        result.putIfAbsent(key, 0);
                        if (value1.query(value).size() != 0) {
                            result.put(key, result.get(key) + 1);
                        }
                    });
                }
                break;
            default:
                break;
        }

        return result;
    }


    private static void fillModifyFeatures(FeatureDo featureDo, HashMap<SpecificTreeParser.ResultEnum, Integer> result, String category) {
        var Iterator=result.entrySet().iterator();
        switch (category){
            case "ADD":
                while(Iterator.hasNext()){
                    Map.Entry<SpecificTreeParser.ResultEnum,Integer> entry=Iterator.next();
                    SpecificTreeParser.ResultEnum resultEnum=entry.getKey();
                    int count=entry.getValue();
                    FillAddConcreteCount(featureDo, resultEnum, count);
                }
                break;
            case "DEL":
                while(Iterator.hasNext()){
                    Map.Entry<SpecificTreeParser.ResultEnum,Integer> entry=Iterator.next();
                    SpecificTreeParser.ResultEnum resultEnum=entry.getKey();
                    int count=entry.getValue();
                    FillDelConcreteCount(featureDo, resultEnum, count);
                }
                break;
            default:
                break;
        }

    }

    private static void FillDelConcreteCount(FeatureDo featureDo, SpecificTreeParser.ResultEnum resultEnum, int count) {
        switch (resultEnum){
            case Annotation:
                featureDo.setDel_annotation_line(count);
                break;
            case Expression:
                featureDo.setDel_condition_line(count);
                break;
            case PackageDeclaration:
                featureDo.setDel_packageid_line(count);
                break;
            case ImportDeclaration:
                featureDo.setDel_import_line(count);
                break;
            case NormalClassDeclaration:
                featureDo.setDel_classname_line(count);
                break;
            case FormalParameterList:
                featureDo.setDel_parameter_line(count);
                break;
            case MethodInvocation:
                featureDo.setDel_call_line(count);
                break;
            case ReturnStatement:
                featureDo.setDel_return_line(count);
                break;
            case FieldDeclaration:
                featureDo.setDel_field_line(count);
                break;
            default:
                break;
        }
    }

    private static void FillAddConcreteCount(FeatureDo featureDo, SpecificTreeParser.ResultEnum resultEnum, int count) {
        switch (resultEnum){
            case Annotation:
                featureDo.setAdd_annotation_line(count);
                break;
            case Expression:
                featureDo.setAdd_condition_line(count);
                break;
            case PackageDeclaration:
                featureDo.setAdd_packageid_line(count);
                break;
            case ImportDeclaration:
                featureDo.setAdd_import_line(count);
                break;
            case NormalClassDeclaration:
                featureDo.setAdd_classname_line(count);
                break;
            case FormalParameterList:
                featureDo.setAdd_parameter_line(count);
                break;
            case MethodInvocation:
                featureDo.setAdd_call_line(count);
                break;
            case ReturnStatement:
                featureDo.setAdd_return_line(count);
                break;
            case FieldDeclaration:
                featureDo.setAdd_field_line(count);
                break;
            default:
                break;
        }
    }

    private static void fillFeatures(FeatureDo featureDo, HashMap<SpecificTreeParser.ResultEnum, IntervalTree<Integer>> IntervalTree, String category) {
        var Iterator=IntervalTree.entrySet().iterator();

        switch (category){
            case "ADD":
                while(Iterator.hasNext()){
                    Map.Entry<SpecificTreeParser.ResultEnum,IntervalTree<Integer>> entry=Iterator.next();
                    SpecificTreeParser.ResultEnum resultEnum=entry.getKey();
                    IntervalTree tree=entry.getValue();
                    int count=getCountNumber(tree);
                    FillAddConcreteCount(featureDo, resultEnum, count);
                }
                break;
            case "DEL":
                while(Iterator.hasNext()){
                    Map.Entry<SpecificTreeParser.ResultEnum,IntervalTree<Integer>> entry=Iterator.next();
                    SpecificTreeParser.ResultEnum resultEnum=entry.getKey();
                    IntervalTree tree=entry.getValue();
                    int count=getCountNumber(tree);
                    FillDelConcreteCount(featureDo, resultEnum, count);
                }
                break;
            default:
                break;
        }

    }

    private static int getCountNumber(IntervalTree<Integer> tree) {
        return tree.stream().mapToInt(x->(x.getEnd()-x.getStart())+1).sum();
    }


    public static int getIndex(String commitID, List<CommitMessage> commitMessages){
        //转入自本文件189行，传入commitId和历史commit信息列表，返回此次commit的索引

        for(int i=0;i<commitMessages.size();i++){
            if(commitID.equals(commitMessages.get(i).getCommitId())){
                return i;
            }
        }
        System.out.println("not found index");
        return -1;
    }

    public static String readContents(File file){
        char[] contents=new char[(int)file.length()];
        try(FileReader reader=new FileReader(file)){
            reader.read(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(contents);
    }

    public static String[] extractImportLines(String multiLineString) {
        String[] lines = multiLineString.split("\\r?\\n");
        return java.util.Arrays.stream(lines)
                .map(String::trim)                 // 去掉前后空格
                .filter(line -> line.startsWith("import")) // 过滤 import 开头的行
                .toArray(String[]::new);           // 转换为数组
    }

    public static String[] findDifferences(String[] array1, String[] array2) {
        List<String> list1 = Arrays.asList(array1);
        List<String> list2 = Arrays.asList(array2);

        List<String> result = new ArrayList<>();

        for (String str : array1) {
            if (!list2.contains(str)) {
                result.add(str);
            }
        }

        for (String str : array2) {
            if (!list1.contains(str)) {
                result.add(str);
            }
        }
        return result.toArray(new String[0]);
    }

    public static Boolean hasIntersection(String[] array1, String[] array2) {
        Set<String> set1 = new HashSet<>();
        for (String str : array1) {
            set1.add(str);
        }

        for (String str : array2) {
            if (set1.contains(str)) {
                return true;
            }
        }
        return false;
    }

    public static String extractChanges(String original, String modified) {
        String originalWithoutComments = removeComments(original);
        String modifiedWithoutComments = removeComments(modified);

        StringBuilder changes = new StringBuilder();
        String[] originalLines = originalWithoutComments.split("\n");
        String[] modifiedLines = modifiedWithoutComments.split("\n");

        for (String line : modifiedLines) {
            if (!containsLine(originalLines, line)) {
                changes.append("[+] ").append(line.trim()).append("\n");
            }
        }
        for (String line : originalLines) {
            if (!containsLine(modifiedLines, line)) {
                changes.append("[-] ").append(line.trim()).append("\n");
            }
        }
        return changes.toString();
    }

    private static boolean containsLine(String[] lines, String target) {
        for (String line : lines) {
            if (line.trim().equals(target.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String removeComments(String code) {
        String normalizedCode = code.replaceAll("\r\n", "\n");
        String[] lines = normalizedCode.split("\n");
        StringBuilder result = new StringBuilder();
        boolean insideBlockComment = false;

        for (String line : lines) {
            if (insideBlockComment) {
                int blockCommentEnd = line.indexOf("*/");
                if (blockCommentEnd != -1) {
                    insideBlockComment = false;
                    line = line.substring(blockCommentEnd + 2);  // 删除注释结束符后部分
                } else {
                    continue;
                }
            }

            int blockCommentStart = line.indexOf("/*");
            if (blockCommentStart != -1) {
                insideBlockComment = true;
                line = line.substring(0, blockCommentStart);
            }

            int singleLineCommentStart = line.indexOf("//");
            if (singleLineCommentStart != -1) {
                line = line.substring(0, singleLineCommentStart);  // 删除单行注释部分
            }

            if (!line.trim().isEmpty()) {
                result.append(line).append("\n");
            }
        }
        return result.toString().trim();
    }


    public static boolean isSemanticallyRelated(String str1, String str2) {
        String processedStr1 = removeBracketsContent(str1);
        String processedStr2 = removeBracketsContent(str2);

        Set<String> words1 = tokenize(processedStr1);
        Set<String> words2 = tokenize(processedStr2);

        words1.retainAll(words2);
        return !words1.isEmpty();
    }

    private static String removeBracketsContent(String str) {
        return str.replaceAll("\\[\\+\\]", "").replaceAll("\\[\\-\\]", "").trim();
    }

    private static Set<String> tokenize(String str) {
        String[] words = str.split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }

    public static boolean areAllFinal(List<String> list) {
        for (String s : list) {
            if (!"final".equals(s)) {
                return false;
            }
        }
        return true;
    }

}




