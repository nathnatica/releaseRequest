package com.github.tester.util;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

public class SVNUtil {
    final static Logger logger = LoggerFactory.getLogger(SVNUtil.class);
    
    public static List<File> getModifiedFileList(String path) {

        SVNClientManager svnClientManager = SVNClientManager.newInstance();
        final List<File> fileList = new ArrayList<File>();
        try {
            svnClientManager.getStatusClient().doStatus(new File(path), SVNRevision.BASE, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
                @Override
                public void handleStatus(SVNStatus svnStatus) throws SVNException {
                    SVNStatusType statusType = svnStatus.getContentsStatus();
                    if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL && statusType != SVNStatusType.STATUS_IGNORED) {
                        fileList.add(svnStatus.getFile());
                    }
                }
            }, null);
        } catch (SVNException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return fileList;
    }
    
    public static SVNRepository getSVNRepository(String url, String name, String pass) {
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, pass);
            repository.setAuthenticationManager(authManager);
        } catch (SVNException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return repository;
    }
    
    public static Collection<SVNLogEntry> getLogEntitiesFrom(SVNRepository repository, Date date) {
        Collection<SVNLogEntry> logEntities = null;
        long startRevision = 0;
        try {
            startRevision = repository.getDatedRevision(date);
            long endRevision = repository.getLatestRevision();
            logEntities = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return logEntities;
    }
    
    public static Set<String> getSortedChangedPaths(SVNLogEntry logEntry) {
        Set<String> changedPathSet = logEntry.getChangedPaths().keySet();
        Set<String> sortedSet = new TreeSet<String>();
        sortedSet.addAll(changedPathSet);
        return sortedSet;
    }
    
    
    
}
