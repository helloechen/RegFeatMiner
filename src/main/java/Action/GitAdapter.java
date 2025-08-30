package Action;

import Obj.CommitMessage;
import Resource.Resource;
import com.gitblit.models.PathModel;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.*;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GitAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GitAdapter.class);

    private final static String REF_HEADS = "refs/heads/";
    private final static String MASTER_BRANCH = "master";
    private final String remotePath;

    private String localPath;

    private String localGitPath;

    private Git git;

    private Repository repository;

    public String branchName;
    private static UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider;


    public GitAdapter(String remotePath, String localPath) {
        this(remotePath, localPath, MASTER_BRANCH);
    }

    public GitAdapter(String remotePath, String localPath, String branchName) {
        //从Utility/gitInstance 49行跳转而来，详细看GitAdapter的构造过程
        //remotePath: https://github.com/apache/{仓库名}.git
        //localPath: java_data/{仓库名}

        this.remotePath = remotePath;
        System.out.println(remotePath);
        this.localPath = localPath;
        this.branchName = branchName;
        localGitPath = this.localPath + "/.git";
        String privateToken = Resource.githubToken;

        this.usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN",
                privateToken);
        //这个GitAdapter利用第三方jar包，利用privateToken创建了一个usernamePasswordCredentialsProvider作为成员变量
    }


    public Git initGit() {
        //gitAdapter在构造函数之后有以下成员变量可用：
        //remotePath: https://github.com/apache/{仓库名}.git
        //localPath: java_data/{仓库名}
        //branchName:写死成master了
        //localGitPath:java_data/{仓库名}/.git
        //一个usernamePasswordCredentialsProvider对象
        File file = new File(localPath);
        //用localPath:java_data/{仓库名}创建一个File对象就叫file
        //若某个仓库是第一次碰见，那么file还没有实例，进入else。否则若非初见，进入if



        if (file.exists()) {
            //这个仓库不是第一次遇见了，先前已经clone过了
            try {
                git = Git.open(new File(localPath));
                System.out.println(git);
            //因为已经clone过此仓库了，把仓库打开成为引用git(补充了adapter成员变量git的空白)，打印一下仓库基本信息就可以了
            } catch (IOException e) {
                logger.info("open failure");
                e.printStackTrace();
            }

        } else {
            try {
                git = Git.cloneRepository()
                        .setCredentialsProvider(usernamePasswordCredentialsProvider)
                        .setURI(remotePath)
                        .setTimeout(50)
                        .setBranch(branchName)
                        .setDirectory(new File(localPath))
                        .call();
                git.pull().setCredentialsProvider(usernamePasswordCredentialsProvider).call();
                logger.info("down success");


            } catch (GitAPIException e) {
                logger.error("download_error");
                e.printStackTrace();
            }
        }
        repository = git.getRepository();
        return git;
    }


    public Ref getBranchRef() throws IOException {
        return getBranchRef(this.branchName);
    }


    public Ref getBranchRef(String branchName) {
        try {
            return git.getRepository().exactRef(REF_HEADS + branchName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getTestTime(String testsha1){
        try{
            RevCommit commit = git.log()
                    .add(repository.resolve(testsha1))
                    .call()
                    .iterator()
                    .next();

            int commitTime = commit.getCommitTime();
            long commitTimeMillis = commitTime * 1000L;

            Date commitDate = new Date(commitTimeMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(commitDate);
            return formattedDate;
        }
        catch (Exception e){
            System.out.println("getTestTime failed");
            return null;
        }
    }


    public String getBranchSpecificFileContent(String branchName, String javaPath) throws IOException {
        Ref branch = this.getBranchRef(branchName);
        ObjectId objId = branch.getObjectId();
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(objId);
        return getFileContent(javaPath, tree, walk, "UTF-8");
    }

    public List<String> findChangedFile(String commitID,String parentCommitID) {
        List<String> changedFiles = new ArrayList<>();
        RevWalk revWalk = new RevWalk(repository);
        try {
            RevCommit commit = revWalk.parseCommit(repository.resolve(commitID));
            RevCommit parentCommit = revWalk.parseCommit(repository.resolve(parentCommitID));

            if (parentCommit != null) {
                List<DiffEntry> diffEntries = new Git(repository).diff()
                        .setOldTree(prepareTreeParser(repository, parentCommit))
                        .setNewTree(prepareTreeParser(repository, commit))
                        .call();

                for (DiffEntry entry : diffEntries) {
                    if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                        changedFiles.add(entry.getOldPath());
                    } else {
                        changedFiles.add(entry.getNewPath());
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return changedFiles;
    }

    public org.eclipse.jgit.treewalk.AbstractTreeIterator prepareTreeParser(Repository repository, RevCommit commit) throws IOException {
        try {
            org.eclipse.jgit.treewalk.CanonicalTreeParser treeParser = new org.eclipse.jgit.treewalk.CanonicalTreeParser();
            ObjectReader reader = repository.newObjectReader();
            treeParser.reset(reader, commit.getTree().getId());
            return treeParser;
        } catch (IOException e) {
            throw new IOException("Error preparing tree parser for commit: " + commit.getId().getName(), e);
        }
    }



    public String getCommitSpecificFileContent(String Commitment, String javaPath) throws IOException {
        RevWalk walk = new RevWalk(repository);
        var tree = walk.parseTree(walk.parseCommit(repository.resolve(Commitment)).getTree().getId());
        return getFileContent(javaPath, tree, walk, "UTF-8");
    }

    private String getFileContent(String javaPath, RevTree tree, RevWalk walk, String charsets) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
        if (treeWalk == null) {
            boolean flag = false;
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            ObjectReader reader = repository.newObjectReader();
            treeParser.reset(reader, tree.getId());
            ObjectReader objectReader = repository.newObjectReader();
            treeWalk = new TreeWalk(objectReader);
            treeWalk.addTree(treeParser);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                if (treeWalk.getPathString().equals(javaPath)) {
                    flag = true;
                    break;
                }
            }
            assert flag;
        }

        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(blobId, Constants.OBJ_BLOB);
        byte[] bytes = loader.getBytes();
        walk.dispose();
        return new String(bytes, charsets);
    }


    public AbstractTreeIterator prepareTreeParser(Ref localRef) throws IOException {
        return prepareTreeParser(localRef.getObjectId().getName());
    }


    public AbstractTreeIterator prepareTreeParser(String commitId) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevCommit revCommit = walk.parseCommit(repository.resolve(commitId));
        return prepareTreeParser(revCommit);
    }

    public AbstractTreeIterator prepareTreeParser(RevCommit revCommit) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(revCommit.getTree().getId());
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectReader reader = repository.newObjectReader();
        treeParser.reset(reader, tree.getId());
        walk.dispose();
        return treeParser;
    }


    public void checkOut(String branchName) throws GitAPIException {

        git.checkout().setCreateBranch(false).setName(branchName).call();

    }


    public void checkOut(String commitID, String branchName) throws GitAPIException {
        git.checkout().setCreateBranch(true).setName(branchName).setStartPoint(commitID).call();
    }


    public Ref checkOutAndPull(String branchName) throws GitAPIException {
        Ref branchRef = getBranchRef(branchName);
        boolean isCreateBranch = branchRef == null;
        if (!isCreateBranch && checkBranchNewVersion(branchRef)) {
            return branchRef;
        }
        git.checkout().setCreateBranch(isCreateBranch).setName(branchName)
                .setStartPoint("origin/" + branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .call();
        git.pull().setCredentialsProvider(usernamePasswordCredentialsProvider).call();
        branchRef = getBranchRef(branchName);
        return branchRef;
    }


    public void checkoutFromVersionCommit(String versionCommit) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        ObjectId versionId = repository.resolve(versionCommit);
        RevCommit verCommit = walk.parseCommit(versionId);
        git.checkout().setCreateBranch(false).setName(versionCommit).setStartPoint(verCommit).call();
        git.branchDelete().setBranchNames(versionCommit);

        String ref = git.getRepository().getRefDatabase().getRefs().toString();
        System.out.println(ref);
    }


    private boolean checkBranchNewVersion(Ref localRef) throws GitAPIException {
        String localRefName = localRef.getName();
        String localRefObjectId = localRef.getObjectId().getName();
        Collection<Ref> remoteRefs = git.lsRemote().setCredentialsProvider(usernamePasswordCredentialsProvider).setHeads(true).call();
        for (Ref remoteRef : remoteRefs) {
            String remoteRefName = remoteRef.getName();
            String remoteRefObjectId = remoteRef.getObjectId().getName();
            if (remoteRefName.equals(localRefName)) {
                return remoteRefObjectId.equals(localRefObjectId);
            }
        }
        return false;
    }


    public List<CommitMessage> getMergeCommitMessages(int n) throws IOException, GitAPIException {
        List<CommitMessage> commitMessages = new ArrayList<>();
        CommitMessage commitMessage;
        Iterable<RevCommit> commits = git.log().all().call();
        RevWalk walk = new RevWalk(repository);
        int flag = 0;
        for (RevCommit commit : commits) {
            commitMessage = new CommitMessage();
            boolean foundInThisBranch = false;
            RevCommit targetCommit = walk.parseCommit(commit.getId());
            for (Map.Entry<String, Ref> e : repository.getAllRefs().entrySet()) {
                if (e.getKey().startsWith("refs/remotes/origin")) {
                    if (walk.isMergedInto(targetCommit, walk.parseCommit(e.getValue().getObjectId()))) {
                        String foundInBranch = e.getValue().getTarget().getName();
                        if (foundInBranch.contains(branchName)) {
                            if (targetCommit.getParents().length == 2) {
                                flag++;
                                foundInThisBranch = true;
                                break;
                            }
                        }
                    }
                }

            }
            if (foundInThisBranch) {
                commitMessage.setCommitId(commit.getName());
                commitMessage.setCommitIdent(commit.getAuthorIdent().getName());
                commitMessage.setCommitMessage(commit.getFullMessage());
                commitMessage.setCommitDate(new Date(commit.getCommitTime() * 1000L).toString());
                commitMessage.setLastCommitId(commit.getParent(0).getName());
                commitMessage.setMergeBranchCommitId(commit.getParent(1).getName());
                commitMessages.add(commitMessage);
            }
            if (flag == n) {
                break;
            }

        }
        return commitMessages;
    }

    public List<CommitMessage> getNo_MergeCommitMessages() throws IOException, GitAPIException {

        //从/Utility/matchUtility 156行转入，使用GitAdapter调取对应仓库的提交记录，返回回去
        List<CommitMessage> commitMessages = new ArrayList<>();
        CommitMessage commitMessage;
        //准备好要返回的列表commitMessages和辅助变量commitMessage


        Iterable<RevCommit> commits = git.log().setRevFilter(RevFilter.NO_MERGES)
                .call();
        //获得一个含有该仓库所有非merge提交的commit迭代器，叫commits


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //创建标准日期格式

        for (RevCommit commit : commits) {
            commitMessage = new CommitMessage();
            commitMessage.setCommitId(commit.getName());
            commitMessage.setCommitIdent(commit.getAuthorIdent().getName());
            commitMessage.setCommitMessage(commit.getFullMessage());
            commitMessage.setCommitDate(dateFormat.format(new Date(commit.getCommitTime() * 1000L)));
            //从迭代器里拿到每个commit的信息，对应创建CommitMessage对象，并设置上CommitId, 作者，信息，以秒为单位的提交时间戳


            if (commit.getParentCount() != 0) {
                commitMessage.setLastCommitId(commit.getParent(0).getName());
            }
            //每次commit的若有父提交，将索引为0的父提交的CommitId也记录到这次提交的commitMessage中，叫LastCommitID



            commitMessages.add(commitMessage);
            //处理好的commitMessage添加到返回列表中
        }
        return commitMessages;
        //返回本仓库所有非merge提交的commitMessages(是个列表)

    }

    public void grepPrintingResults(String revName, Pattern pattern) {
        ObjectReader objectReader = repository.newObjectReader();
        try {
            ObjectId commitID = repository.resolve(revName);
            impl(objectReader, commitID, pattern);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void impl(ObjectReader objectReader, ObjectId commitID, Pattern pattern) throws IOException {
        var treeWalk = new TreeWalk(objectReader);
        var revWalk = new RevWalk(objectReader);
        var commit = revWalk.parseCommit(commitID);
        var treeParser = new CanonicalTreeParser();
        treeParser.reset(objectReader, commit.getTree());
        int treeIndex = treeWalk.addTree(treeParser);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            AbstractTreeIterator it = treeWalk.getTree(treeIndex,
                    AbstractTreeIterator.class);
            ObjectId objectId = it.getEntryObjectId();
            ObjectLoader objectLoader = objectReader.open(objectId);

            if (!isBinary(objectLoader.openStream())) {
                List<String> matchedLines = getMatchedLines(objectLoader.openStream(), pattern);
                if (!matchedLines.isEmpty()) {
                    String path = it.getEntryPathString();
                    for (String matchedLine : matchedLines) {
                        System.out.println(path + ":" + matchedLine);
                    }
                }
            }
        }
    }


    private List<String> getMatchedLines(ObjectStream openStream, Pattern pattern) {
        BufferedReader buf;
        List<String> matchedLines = null;
        try {
            matchedLines = new ArrayList<>();
            buf = new BufferedReader(new InputStreamReader(openStream, StandardCharsets.UTF_8));
            String line;
            while ((line = buf.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    matchedLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matchedLines;
    }

    private boolean isBinary(ObjectStream openStream) throws IOException {
        try {
            return RawText.isBinary(openStream);
        } finally {
            try {
                openStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public List<String> getJavaFilesCommit(String revName) {
        return this.getFilesCommit(revName, "^.*(.java)$");
    }

    public List<String> getFilesCommit(String revName, String regrex) {
        List<String> filesNames = null;
        try {
            filesNames = new ArrayList<>();
            ObjectId commitID = this.repository.resolve(revName);
            ObjectReader objectReader = repository.newObjectReader();

            TreeWalk treeWalk = new TreeWalk(objectReader);
            RevWalk revWalk = new RevWalk(objectReader);
            RevCommit commit = revWalk.parseCommit(commitID);
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            treeParser.reset(objectReader, commit.getTree());
            treeWalk.addTree(treeParser);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                String line = treeWalk.getPathString();
                if (Pattern.matches(regrex, line)) {
                    filesNames.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesNames;
    }


    public RevCommit getRevCommit(String CommitString) throws IOException {
        RevWalk walk = new RevWalk(this.repository);
        ObjectId commitID = this.repository.resolve(CommitString);
        ObjectReader objectReader = repository.newObjectReader();

        RevWalk revWalk = new RevWalk(objectReader);
        return revWalk.parseCommit(commitID);

    }


    static String getClassContent(GitAdapter gitAdapter, String Name, String JavaPath, boolean isCommit) {
        String classInfo = null;
        if (!isCommit) {
            try {
                classInfo = gitAdapter.getBranchSpecificFileContent(Name, JavaPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                classInfo = gitAdapter.getCommitSpecificFileContent(Name, JavaPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classInfo;
    }


    public List<DiffEntry> getFilesInRange(String BeforeCommit, String endCommit) {
        //转入自GitAdapter 733行
        //BeforeCommit：此次commit前首个父提交的commitID
        //endCommit：此次commit的ID

        List<DiffEntry> list = new ArrayList<DiffEntry>();
        //利用第三方jar包，创建一个DiffEntry对象的容器

        try {
            ObjectId startRange = this.repository.resolve(BeforeCommit);
            ObjectId endRange = repository.resolve(endCommit);
            //利用adapter先前设置好的仓库成员变量，为BeforeCommit和endCommit创建各自的ObjectId


            try (var rw = new RevWalk(repository)) {
                RevCommit start = rw.parseCommit(startRange);
                RevCommit end = rw.parseCommit(endRange);
                //利用repository创建一个RevWalk叫rw
                //把BeforeCommit和endCommit都解析成RevCommit对象
                list.addAll(getFilesInRange(repository, start, end));
                //把BeforeCommit和endCommit解析成的RevCommit对象和仓库一起传入getFilesInRange，返回的diffEntry全部添加进list
                //-------+++++跳转至gitAdapter的514行++++++----------------------------------
                //回来看，其将此次提交与索引为0的父提交的所有区别对比出来了，并且返回添加进入了List

            }
        } catch (Throwable t) {
            System.out.println("different list failed");
        }
        return list;
    }


    private List<DiffEntry> getFilesInRange(Repository repository, RevCommit startCommit, RevCommit endCommit) {
        //转入自gitAdapter504行
        //repository：项目仓库
        //startCommit，endCommit分别代表此次提交和上一次索引为0的父提交

        List<DiffEntry> diffEntries = null;
        //创建一个辅助用的DiffEntry列表

        try (var df = CodeDiff.getDiffFormatter(false, DisabledOutputStream.INSTANCE, repository)) {

            diffEntries = df.scan(prepareTreeParser(startCommit), prepareTreeParser(endCommit));

        } catch (Throwable t) {
        }

        return diffEntries;
        //调用api,提取出两个提交之间的所有差异条目，并进行返回
    }


    public static List<PathModel> getDocuments(Repository repository, List<String> extensions, String CommitID) throws IOException {
        List<PathModel> list = new ArrayList<PathModel>();
        var walk = new RevWalk(repository);
        ObjectId commitID = repository.resolve(CommitID);
        RevCommit commit = walk.parseCommit(commitID);
        try (var tw = new TreeWalk(repository)) {
            tw.addTree(commit.getTree());
            if (extensions != null && extensions.size() > 0) {
                List<TreeFilter> suffixFilters = new ArrayList<TreeFilter>();
                for (String extension : extensions) {
                    if (extension.charAt(0) == '.') {
                        suffixFilters.add(PathSuffixFilter.create(extension));
                    } else {
                        suffixFilters.add(PathSuffixFilter.create("." + extension));
                    }
                }
                TreeFilter filter;
                if (suffixFilters.size() == 1) {
                    filter = suffixFilters.get(0);
                } else {
                    filter = OrTreeFilter.create(suffixFilters);
                }
                tw.setFilter(filter);
                tw.setRecursive(true);
            }
            while (tw.next()) {
                list.add(getPathModel(tw, null, commit));
            }
        } catch (IOException e) {

        }
        Collections.sort(list);
        return list;
    }


    private static PathModel getPathModel(TreeWalk tw, String basePath, RevCommit commit) {
        String name;
        long size = 0;
        if (basePath == null && basePath.trim().equals("")) {
            name = tw.getPathString();
        } else {
            name = tw.getPathString().substring(basePath.length() + 1);
        }
        var objectId = tw.getObjectId(0);
        try {
            if (!tw.isSubtree() && (tw.getFileMode(0) != FileMode.GITLINK)) {
                size = tw.getObjectReader().getObjectSize(objectId, Constants.OBJ_BLOB);
            }
        } catch (Throwable t) {
            System.out.println(t.toString());
        }
        return new PathModel(name, tw.getPathString(), size, tw.getFileMode(0).getBits(), objectId.getName(), commit.getName());
    }


    public List<RevCommit> getRevLog(String objectId, Date minimumDate) {
        List<RevCommit> list = new ArrayList<RevCommit>();

        try {
            ObjectId branchObject = repository.resolve(objectId);

            var rw = new RevWalk(repository);
            rw.markStart(rw.parseCommit(branchObject));
            rw.setRevFilter(CommitTimeRevFilter.after(minimumDate));
            Iterable<RevCommit> revlog = rw;
            for (RevCommit rev : revlog) {
                list.add(rev);
            }
            rw.close();
            rw.dispose();
        } catch (Throwable t) {
        }
        return list;
    }


    public List<RevCommit> getRevLog(int maxCount) {
        return getRevLog(null, 0, maxCount);
    }


    public List<RevCommit> getRevLog(String objectId, int offset, int maxCount) {
        return getRevLog(objectId, null, offset, maxCount);
    }


    public List<RevCommit> getRevLog(String objectId, String path, int offset, int maxCount) {
        List<RevCommit> list = new ArrayList<RevCommit>();
        if (maxCount == 0) {
            return list;
        }

        try {
            ObjectId startRange = null;
            ObjectId endRange;
            if (objectId.contains("..")) {
                String[] parts = objectId.split("\\.\\.");
                startRange = repository.resolve(parts[0]);
                endRange = repository.resolve(parts[1]);
            } else {
                endRange = repository.resolve(objectId);
            }

            if (endRange == null) {
                return list;
            }

            var rw = new RevWalk(repository);
            rw.markStart(rw.parseCommit(endRange));
            if (startRange != null) {
                rw.markUninteresting(rw.parseCommit(startRange));
            }
            if (path != null && path.trim().equals("")) {
                var filter = AndTreeFilter.create(PathFilterGroup.createFromStrings(Collections.singleton(path)), TreeFilter.ANY_DIFF);
                rw.setTreeFilter(filter);
            }
            Iterable<RevCommit> revlog = rw;
            if (offset > 0) {
                var count = 0;
                for (RevCommit rev : revlog) {
                    count++;
                    if (count > offset) {
                        list.add(rev);
                        if (maxCount > 0 && list.size() == maxCount) {
                            break;
                        }
                    }
                }
            } else {
                for (RevCommit rev : revlog) {
                    list.add(rev);
                    if (maxCount > 0 && list.size() == maxCount) {
                        break;
                    }
                }
            }
            rw.close();
            rw.dispose();
        } catch (Throwable t) {
        }
        return list;
    }

    public List<RevCommit> getRevLog(String startRangeId, String endRangeId) {
        List<RevCommit> list = new ArrayList<RevCommit>();

        try {
            ObjectId endRange = repository.resolve(endRangeId);
            ObjectId startRange = repository.resolve(startRangeId);

            var rw = new RevWalk(repository);
            rw.markStart(rw.parseCommit(endRange));
            if (startRange.equals(ObjectId.zeroId())) {
                list.add(rw.parseCommit(endRange));
                rw.close();
                rw.dispose();
                return list;
            } else {
                rw.markUninteresting(rw.parseCommit(startRange));
            }

            Iterable<RevCommit> revlog = rw;
            for (RevCommit rev : revlog) {
                list.add(rev);
            }
            rw.close();
            rw.dispose();
        } catch (Throwable t) {
            System.out.println(t.toString());
        }
        return list;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public String getProjectName() {
        return localPath.substring(this.localPath.lastIndexOf("/") + 1);
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }


    public DiffEntry getDiffOfFileInCommit(CommitMessage commitMessage, String Path) throws IOException {

        System.out.println("pro_commit "+commitMessage.getCommitId());
        var diffEntries = this.getFilesInRange(commitMessage.getLastCommitId(), commitMessage.getCommitId());

        for (DiffEntry diffEntry : diffEntries) {
            String path = diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE ? diffEntry.getOldPath() : diffEntry.getNewPath();
            if (path.equals(Path) && (CodeDiff.getDiffFormatter(true, null, this.repository)
                    .toFileHeader(diffEntry).toEditList().size() != 0)) {
                return diffEntry;
            }
        }
        return null;
    }
}