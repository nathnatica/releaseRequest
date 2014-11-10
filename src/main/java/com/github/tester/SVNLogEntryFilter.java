package com.github.tester;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.*;

public class SVNLogEntryFilter {
    final static Logger logger = LoggerFactory.getLogger(SVNLogEntryFilter.class);
    
    public static List<SVNLogEntry> filter(Collection<SVNLogEntry> logEntries, String checkAccount, String checkMessage, Date checkDate) {
    
        List<SVNLogEntry> l = new ArrayList<SVNLogEntry>();
        
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            boolean target = true;
            
            if (logEntry.getDate().before(checkDate)) {
                target = false;
            }
            
            if (StringUtils.isNotBlank(checkAccount) && (!checkAccount.contains(logEntry.getAuthor()))) {
                target = false;
            }

            if (StringUtils.isNotBlank(checkMessage) && (!logEntry.getMessage().contains(checkMessage))) {
                target = false;
            }

            if (target) {
                logger.info("-------------------------------");
                logger.info("revision    " + logEntry.getRevision());
                logger.info("author      " + logEntry.getAuthor());
                logger.info("date        " + logEntry.getDate());
                logger.info("log message " + logEntry.getMessage());
                logger.info("-------------------------------");
                l.add(0, logEntry);
            } else {
                logger.info("[REVISION IGNORED] {}::{}::{}", new Object[]{logEntry.getRevision(), logEntry.getAuthor(), logEntry.getMessage()});
            }

        }
        return l;
    }
    
}
