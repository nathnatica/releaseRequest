package com.github.tester;

import com.github.tester.util.SVNUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.github.tester.util.ResourceBundleUtil.get;

public class ModifiedFileFilter {
    final static Logger logger = LoggerFactory.getLogger(ModifiedFileFilter.class);

    static Map<String, String> fileRootMap;
    static List<String> excludePathList;
    static List<String> excludeFileList;
    static List<String> excludeExtList;
    
    public static List<File> getModifiedFileList() {
        final List<File> modifiedFileList = new ArrayList<File>();
        
         getSrcRootMap();

        for (String key : fileRootMap.keySet()) {
            String path = fileRootMap.get(key);
            logger.info("##### {} #########################################", "CHECK : " + path);
            List<File> l = SVNUtil.getModifiedFileList(path);

            getExcludePathList(key);
            getExcludeFileList(key);
            getExcludeExtList(key);
            
            excludePathFilter(l, path, excludePathList);
            excludeFileFilter(l, excludeFileList);
            excludeExtensionFilter(l, excludeExtList);
            modifiedFileList.addAll(l);
        }
        
        return modifiedFileList;
    }
    
    
    public static void excludePathFilter(List<File> fileList, String prePath, List<String> checkList) {
        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File f = it.next();
            String replaced = f.getAbsolutePath().replace(prePath, StringUtils.EMPTY);
            for (String checkPath : checkList) {
                if (replaced.startsWith(checkPath)) {
                    logger.debug("[MODIFIED BUT IGNORED PATH] {}", f.getAbsolutePath());
                    it.remove();
                }
            }
        }
    }

    public static void excludeFileFilter(List<File> fileList, List<String> checkList) {
        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File f = it.next();
            if (f.isFile()) {
                for (String checkFile : checkList) {
                    if (StringUtils.equals(f.getName(), checkFile)) {
                        logger.debug("[MODIFIED BUT IGNORED FILE] {}", f.getName());
                        it.remove();
                    }
                }
            }
        }
    }

    public static void excludeExtensionFilter(List<File> fileList, List<String> checkList) {
        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File f = it.next();
            if (f.isFile()) {
                for (String checkExt : checkList) {
                    if (f.getName().endsWith(checkExt)) {
                        logger.debug("[MODIFIED BUT IGNORED  EXT] {}", f.getName());
                        it.remove();
                    }
                }
            }
        }
    }

    private static void getExcludeExtList(String key) {
        List<String> excludeExtList = new ArrayList<String>();
        for (int i=1; i<=20; i++) {
            String excludeExt;
            try {
                excludeExt = get("exclude.ext." + key + "." + i);
            } catch (Exception e) {
                logger.debug("[ERROR FINDING PROPERTY] {}", "exclude.ext." + key + "." + i) ;
                break;
            }
            if (excludeExt != null) {
                excludeExtList.add(excludeExt);
                excludeExt = null;
            }
        }
        ModifiedFileFilter.excludeExtList = excludeExtList;
    }

    private static void getExcludeFileList(String key) {
        List<String> excludeFileList = new ArrayList<String>();
        for (int i=1; i<=20; i++) {
            String excludeFile;
            try {
                excludeFile = get("exclude.file." + key + "." + i);
            } catch (Exception e) {
                logger.debug("[ERROR FINDING PROPERTY] {}", "exclude.file." + key + "." + i) ;
                break;
            }
            if (excludeFile != null) {
                excludeFileList.add(excludeFile);
                excludeFile = null;
            }
        }
        ModifiedFileFilter.excludeFileList = excludeFileList;
    }

    private static void getExcludePathList(String key) {
        List<String> excludePathList = new ArrayList<String>();
        String excludePath;
        for (int i=1; i<=20; i++) {
            try {
                excludePath = get("exclude.path." + key + "." + i);
            } catch (Exception e) {
                logger.debug("[ERROR FINDING PROPERTY] {}", "exclude.path." + key + "." + i) ;
                break;
            }
            if (excludePath != null) {
                excludePathList.add(excludePath);
                excludePath = null;
            }
        }
        ModifiedFileFilter.excludePathList = excludePathList;
    }

    private static void getSrcRootMap() {
        Map<String, String> srcRootMap = new LinkedHashMap<String, String>();
        String srcRoot;
        for (int i=1; i<=3; i++) {
            try {
                srcRoot = get("file.root." + i);
            } catch (Exception e) {
                logger.debug("[ERROR FINDING PROPERTY] {}", "file.root." + i) ;
                break;
            }
            if (srcRoot != null) {
                srcRootMap.put(Integer.toString(i), srcRoot);
                srcRoot = null;
            }
        }
        ModifiedFileFilter.fileRootMap = srcRootMap;
    }
}
