package com.github.tester;

import com.github.tester.util.ExcelUtil;
import com.github.tester.util.SVNUtil;
import com.github.tester.util.TimeUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import com.github.tester.util.ResourceBundleUtil;

import static com.github.tester.util.ResourceBundleUtil.get;

import java.io.*;
import java.util.*;

public class TestRunner {
    final static Logger logger = LoggerFactory.getLogger(TestRunner.class);
    
    static AtomicCounter rowCounter;
    
    public static void main(String[] args) throws Exception {

        if (args != null && args.length > 0) {
            ResourceBundleUtil.init(args[0].substring(0, args[0].lastIndexOf(".")));
        }
        TestRunner.rowCounter = new AtomicCounter(Integer.parseInt(get("base.row")));
        rowCounter.getPrevousValue();
        
        setLog();
        
        logger.info("##### {} #########################################", "START");

        String f = get("input.file." + get("site"), false);
        Workbook wb = ExcelUtil.getWorkbook(get("input.folder") + f);

        logger.info("##### {} #########################################", "MAKE FIRST SHEET");
        makeFirstSheet(wb);


        logger.info("##### {} #########################################", "MAKE SVN INFO");
        getSVNInfo(wb); 
        
        if (StringUtils.equals(get("modified.file.check"),"Y")) {
            logger.info("##### {} #########################################", "MAKE MODIFIED FILE INFO");
            List<File> modifiedFileList = ModifiedFileFilter.getModifiedFileList();
            for (File f2 : modifiedFileList) {
                writeRow("???", f2.getPath(), f2.getName(), "update", wb);
            }
        }

        logger.info("##### {} #########################################", "MAKE EXCEL FILE");
        writeWorkbook(wb);

        logger.info("##### {} #########################################", "MAKE MAIL");
        makeMailContent();

        logger.info("##### {} #########################################", "END");
    }


    private static void makeMailContent() {
        String mailMake = get("mail.make");
        if (StringUtils.equalsIgnoreCase(mailMake, "Y")) {
            String template = get("mail");
            try {
                Files.write(template, new File("mail.txt"), Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void getSVNInfo(Workbook wb) throws SVNException {
        DAVRepositoryFactory.setup();

        String url = get("svn.path");
        String name = get("svn.account.id");
        String pass = get("svn.account.pass");

        if (StringUtils.equals("Y", get("make.new.tag"))) {
            logger.info("##### {} #########################################", "MAKE NEW TAG");
            String newTag = SVNUtil.getNewTagName(get("svn.root"), name, pass, get("svn.tag.path"));
            String to = get("svn.root") + "/" + get("svn.tag.path") + "/" + newTag;
            SVNUtil.copy(name, pass, url, to, get("release.issue"));
        }

        SVNRepository repository = SVNUtil.getSVNRepository(url, name, pass);
        
        Date date = TimeUtil.makeDateFromYYYYMMDD(get("svn.check.start.date"));
        Collection logEntities = SVNUtil.getLogEntitiesFrom(repository, date);
        
        String svnCheckAccount = get("svn.check.account");
        String svnCheckMessage = get("svn.check.message");
        
        
        List<SVNLogEntry> filteredList = SVNLogEntryFilter.filter(logEntities, svnCheckAccount, svnCheckMessage);
        
        for (SVNLogEntry logEntry : filteredList) {
            if (logEntry.getChangedPaths().size() > 0) {
                
                String latestTag = SVNUtil.getLatestTag(get("svn.root"), name, pass, get("svn.tag.path"), date, svnCheckMessage);

                Set<String> sortedSet = SVNUtil.getSortedChangedPaths(logEntry);
                
                for (Iterator<String> changedPaths = sortedSet.iterator(); changedPaths.hasNext();) {
                    SVNLogEntryPath entityPath = logEntry.getChangedPaths().get(changedPaths.next());
                    String fullPath = entityPath.getPath();
                    String msg = logEntry.getMessage().replaceAll("\r",StringUtils.EMPTY).replaceAll("\n","");
                    fullPath = fullPath.replace(get("svn.path.trim"), StringUtils.EMPTY);
                    int index = fullPath.lastIndexOf("/");
                    String path = fullPath.substring(0, index+1);
                    String filename = fullPath.substring(index + 1, fullPath.length());
                    char t = entityPath.getType();
                    String type = null;
                    if (t == 'M') {
                        type = "update";
                    } else if (t == 'A') {
                        type = "add";
                    } else if (t == 'D') {
                        type = "delete";
                    }
                    String author = logEntry.getAuthor();
                    String time = logEntry.getDate().toString();
                    
                    if (latestTag == null) {
                        writeRow(msg + "(" + author + ")," + time, path, filename, type, wb);
                    } else {
                        writeRow(latestTag, path, filename, type, wb);
                    }
                }
            }
        }
            
        
    }

    
    private static void writeRow(String msg, String path, String name, String type, Workbook wb) {
        
        logger.debug("[WRITE ROW] {} {} {} {}", new Object[]{msg, path, name, type});
        
        Sheet sheet = wb.getSheet("リリース対象サーバ＆資材一覧");
        Row row = sheet.getRow(TestRunner.rowCounter.getNextValue());
        int cur_col = Integer.parseInt(get("base.col"));
        
        Cell cell1 = row.getCell(cur_col);
        cell1.setCellValue(TestRunner.rowCounter.getValue() - Integer.parseInt(get("base.row")) +1);

        Cell cell2 = row.getCell(++cur_col);
        cell2.setCellValue(get("excel.repo.name"));

        Cell cell3 = row.getCell(++cur_col);
        cell3.setCellValue(msg);

        Cell cell4 = row.getCell(++cur_col);
        cell4.setCellValue(path);
        
        Cell cell5 = row.getCell(++cur_col);
        cell5.setCellValue(name);
        
        Cell cell6 = row.getCell(++cur_col);
        cell6.setCellValue(type);
        
        for (int i=1; i<=3; i++) {
            String key = "excel.group." + i + ".starts.with";
            try {
                String start = get(key);
                if (path.startsWith(start)) {
                    String colList = get("excel.group." + i + ".column");
                    String[] temp = colList.split(",");
                    for (String t : temp) {
                        Cell c = row.getCell(ExcelUtil.getCellColumn(t));
                        c.setCellValue("●");
                    }
                }
            } catch (Exception e) {
                logger.warn(e.getMessage() + e);
                break;
            }
        }
    }
    
    
    private static void writeWorkbook(Workbook wb) throws IOException {
		File dir = new File(get("output.dir"));
		dir.mkdirs();
        FileOutputStream fileOut = new FileOutputStream(get("output.dir") + get("output.file"));
        wb.write(fileOut);
        fileOut.close();
    }

    private static void setLog() throws IOException {
        String timestamp = TimeUtil.getYYYYMMDDHH24MISS();
        MDC.put("logname", timestamp);
    }

    private static void makeFirstSheet(Workbook wb) {
        Sheet sheet = wb.getSheet("リリース申請書");
        
        String formattedReleaseDate = get("release.year") + "/" + get("release.month") + "/" + get("release.date");
        ExcelUtil.setValue(get("request.date.loc"), formattedReleaseDate, sheet);

        ExcelUtil.setValue(get("release.date.loc"), formattedReleaseDate, sheet);

        String releaseTime = get("release.hour") + ":" + get("release.minute");
        ExcelUtil.setValue(get("release.time.loc"), releaseTime, sheet);

        String releaseRequestUser = get("release.request.user");
        ExcelUtil.setValue(get("release.request.user.loc"), releaseRequestUser, sheet);
        
        String releaseIssue = get("release.issue");
        ExcelUtil.setValue(get("release.issue.loc"), releaseIssue, sheet);
    }
}
