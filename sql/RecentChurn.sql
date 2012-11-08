/*Create a table for recent churn*/
CREATE TABLE `RecentChurn` (
  `filePath` varchar(100) DEFAULT NULL,
  `authorDate` timestamp NULL DEFAULT NULL,
  `commit` varchar(100) DEFAULT NULL,
  `recentChurn` int(11) DEFAULT NULL,
  `commitList` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB;

/*stored procedure to calculate recent churn*/
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_RECENT_CHURN`(IN TIMEFRAME INTEGER)
BEGIN
/* variable to store the author date from the cursor*/
DECLARE AUTHOR_DATE TIMESTAMP;
/* end condition for the cursor loop*/
declare no_more_rows BOOLEAN;
/*cursor to get all the commit dates for vulnerability introducing commits*/
DECLARE DATELIST CURSOR FOR 
SELECT
    authordate 
from
    RepoLog,
    GitLogFiles,
    CVEToGit
where
    RepoLog.filepath = GitLogFiles.filepath and
    RepoLog.commit = GitLogFiles.commit and
    CVEToGit.filepath = GitLogFiles.filepath and
    CVEToGit.commitIntroduced = GitLogFiles.commit;
/*initialize cursor handler condition to set to true*/
declare continue handler for not found SET no_more_rows := TRUE;
/*need to set this, otherwise mysql won't let purge the table to refresh the 
table with updated records*/
SET SQL_SAFE_UPDATES=0;
/*delete old records from RecentChurn Table*/
delete from RecentChurn;
/*basically what were doing here is, for each commit on that date and date-TIMEFRAME, fetch the churn each file has undergone*/
/*exit when no more rows to parse*/
OPEN DATELIST;
loopDate: LOOP
    FETCH DATELIST INTO AUTHOR_DATE;
    insert into httpdhistory.RecentChurn select 
        RepoLog.filepath,
        authordate,
        RepoLog.commit,
        Sum(GitLogFiles.LinesInserted) + Sum(GitLogFiles.LinesDeleted) as RecentChurn,
        GROUP_CONCAT(GitLogFiles.commit)
    from
        RepoLog,
        GitLogFiles
    where
        RepoLog.filepath = GitLogFiles.filepath and 
        RepoLog.authordate > AUTHOR_DATE and 
        RepoLog.authordate < TIMESTAMPADD(DAY,TIMEFRAME,AUTHOR_DATE) AND 
        RepoLog.commit = GitLogFiles.commit
    Group by RepoLog.filepath;
    if no_more_rows then
        close datelist;
        leave loopDate;
    end if;
END LOOP loopDate;
END;
