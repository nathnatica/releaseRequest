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

    private static void getSVNInfo(Workbook wb) {
        DAVRepositoryFactory.setup();

        String url = get("svn.path");
        String name = get("svn.account.id");
        String pass = get("svn.account.pass");

        SVNRepository repository = SVNUtil.getSVNRepository(url, name, pass);
        
        Date date = TimeUtil.makeDateFromYYYYMMDD(get("svn.check.start.date"));
        Collection logEntities = SVNUtil.getLogEntitiesFrom(repository, date);
        
        String svnCheckAccount = get("svn.check.account");
        String svnCheckMessage = get("svn.check.message");
        
        
        List<SVNLogEntry> filteredList = SVNLogEntryFilter.filter(logEntities, svnCheckAccount, svnCheckMessage);
        
        for (SVNLogEntry logEntry : filteredList) {
            if (logEntry.getChangedPaths().size() > 0) {

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
                    writeRow(msg + "(" + author + ")," + time, path, filename, type, wb);
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
        cell2.setCellValue("SrcWeb_GLB");

        Cell cell3 = row.getCell(++cur_col);
        cell3.setCellValue(msg);

        Cell cell4 = row.getCell(++cur_col);
        cell4.setCellValue(path);
        
        Cell cell5 = row.getCell(++cur_col);
        cell5.setCellValue(name);
        
        Cell cell6 = row.getCell(++cur_col);
        cell6.setCellValue(type);
        
        String front = "front";
        if (path.startsWith(front)) {
            String colList = get("excel." + front + ".column");
            String[] temp = colList.split(",");
            for (String t : temp) {
                Cell c = row.getCell(ExcelUtil.getCellColumn(t));
                c.setCellValue("●");
            }
        }

        String central = "central";
        if (path.startsWith(central)) {
            String colList = get("excel." + central + ".column");
            String[] temp = colList.split(",");
            for (String t : temp) {
                Cell c = row.getCell(ExcelUtil.getCellColumn(t));
                c.setCellValue("●");
            }
        }

        String admin = "admin";
        if (path.startsWith(admin)) {
            String colList = get("excel." + admin + ".column");
            String[] temp = colList.split(",");
            for (String t : temp) {
                Cell c = row.getCell(ExcelUtil.getCellColumn(t));
                c.setCellValue("●");
            }
        }
        
    }
    
    
    
    private static void makeSheet(File file, Workbook wb) {
//        Sheet sheet = wb.getSheet("リリース対象サーバ＆資材一覧");
//        Row row = sheet.getRow(TestRunner.rowCounter.getNextValue());
//        int cur_col = Integer.parseInt(get("base.col"));
//        Cell cell1 = row.getCell(cur_col);
//        cell1.setCellValue(TestRunner.rowCounter.getValue());
//
//        Cell cell2 = row.getCell(++cur_col);
//        cell2.setCellValue("SrcWeb_GLB");
//        
//        Cell cell3 = row.getCell(++cur_col);
//        cell3.setCellValue("-");
//
//        int index = file.getAbsolutePath().lastIndexOf("\\");
//        String path = file.getAbsolutePath().substring(0, index).replace("D:\\work\\","").replaceAll("\\\\", "/") + "/";
//        String filename = file.getAbsolutePath().substring(index+1, file.getAbsolutePath().length());
//
//
//        Cell cell4 = row.getCell(++cur_col);
//        cell4.setCellValue(path);
//        Cell cell5 = row.getCell(++cur_col);
//        cell5.setCellValue(filename);

        // make maru
//        Cell checkCell = null;
//        if ("SrcC21cm".equals(category)) {
//            if (SI2_ENV) {
//                checkCell = row.getCell(62);
//                checkCell.setCellValue("○");
//            } else if (GIJI_ENV) {
//                checkCell = row.getCell(47);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(48);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(58);
//                checkCell.setCellValue("○");
//            } else if (HONBAN_ENV) {
//                checkCell = row.getCell(12);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(13);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(14);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(15);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(16);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(17);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(18);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(37);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(38);
//                checkCell.setCellValue("○");
//            }
//        } else if ("SrcC21pc".equals(category)) {
//            if (SI2_ENV) {
//                checkCell = row.getCell(62);
//                checkCell.setCellValue("○");
//            } else if (GIJI_ENV) {
//                checkCell = row.getCell(47);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(58);
//                checkCell.setCellValue("○");
//                if (path.contains("SrcC21pc/mall/site/jp/modules")) {
//                    checkCell = row.getCell(48);
//                    checkCell.setCellValue("○");
//                }
//            } else if (HONBAN_ENV) {
//                checkCell = row.getCell(12);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(13);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(14);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(15);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(16);
//                checkCell.setCellValue("○");
//                if (path.contains("SrcC21pc/mall/site/jp/modules")) {
//                    checkCell = row.getCell(17);
//                    checkCell.setCellValue("○");
//                    checkCell = row.getCell(18);
//                    checkCell.setCellValue("○");
//                }
//                checkCell = row.getCell(37);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(38);
//                checkCell.setCellValue("○");
//            }
//        } else if ("SrcC21mb".equals(category)) {
//            if (SI2_ENV) {
//                checkCell = row.getCell(62);
//                checkCell.setCellValue("○");
//            } else if (GIJI_ENV) {
//                checkCell = row.getCell(48);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(58);
//                checkCell.setCellValue("○");
//            } else if (HONBAN_ENV) {
//                checkCell = row.getCell(17);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(18);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(37);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(38);
//                checkCell.setCellValue("○");
//            }
//        } else { // admin
//            if (SI2_ENV) {
//                checkCell = row.getCell(62);
//                checkCell.setCellValue("○");
//            } else if (GIJI_ENV) {
//                checkCell = row.getCell(58);
//                checkCell.setCellValue("○");
//            } else if (HONBAN_ENV) {
//                checkCell = row.getCell(37);
//                checkCell.setCellValue("○");
//                checkCell = row.getCell(38);
//                checkCell.setCellValue("○");
//            }
//        }
    }
    
    private static void writeWorkbook(Workbook wb) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(get("output.file"));
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
