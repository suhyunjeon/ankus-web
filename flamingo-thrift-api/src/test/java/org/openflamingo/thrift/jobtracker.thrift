/*
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Thrift interface for Hadoop JobTracker.
 */

/* Common types and interfaces */
include 'common.thrift'

/*
 * Namespaces for generated code. The idea is to keep code generated by
 * Thrift under a 'hadoop.api' namespace, so that a higher-level set of
 * functions and classes may be defined under 'hadoop'.
 */
namespace cpp     hadoop.api.jobtracker
namespace csharp  Hadoop.API.JobTracker
namespace java    org.apache.hadoop.thriftfs.jobtracker.api
namespace perl    Hadoop.API.jobtracker
namespace php     hadoop_api_jobtracker
namespace py      hadoop.api.jobtracker
namespace rb      Hadoop.API.jobtracker


/*
 * All type names are prefixed with 'Thrift' to avoid confusion when dealing
 * with both thrift and hadoop-land objects with the same name in Java.
 */



enum ThriftTaskType {
  MAP,
  REDUCE,
  JOB_SETUP,
  JOB_CLEANUP,
  TASK_CLEANUP          // What is this?
}

enum ThriftTaskState {
  RUNNING, SUCCEEDED, FAILED, UNASSIGNED, KILLED,
  COMMIT_PENDING, FAILED_UNCLEAN, KILLED_UNCLEAN
}

enum ThriftTaskPhase {
  STARTING, MAP, SHUFFLE, SORT, REDUCE, CLEANUP
}

/**
 * It corresponds to the (inferred) internal state of a TaskInProgress,
 * and not that of a TaskStatus.
 */
enum ThriftTaskQueryState {
  SUCCEEDED,
  FAILED,
  RUNNING,      /* Inferred - Only if startTime is set */
  PENDING,      /* Inferred - Only if startTime is not set */
  KILLED
}

/** Possible job priorities (see ThriftJobStatus) */
enum ThriftJobPriority {
  VERY_HIGH,
  HIGH,
  NORMAL,
  LOW,
  VERY_LOW
}

/** Unique identifier for each job */
struct ThriftJobID {
  /** Unique id of jobtracker */
  1: string jobTrackerID
  /** Unique (to JT) job id */
  2: i32 jobID

  /** Flattened as a string */
  3: string asString
}


/** Description of a job queue */
struct ThriftJobQueueInfo {
  1: string queueName
  2: string schedulingInfo
}

struct ThriftJobQueueList {
  1: list<ThriftJobQueueInfo> queues
}

/** Counter which represents some custom job metric */
struct ThriftCounter {
  1: string name
  2: string displayName
  3: i64 value
}

/** Counters are organized by group */
struct ThriftCounterGroup {
  1: string name
  2: string displayName
  3: map<string, ThriftCounter> counters
}

/** Container structure for counter groups */
struct ThriftGroupList {
  1: list<ThriftCounterGroup> groups
}

/** Counters for map tasks only, reduce tasks only, and job-scoped counters */
struct ThriftJobCounterRollups {
  1: ThriftGroupList mapCounters; // eg map input bytes
  2: ThriftGroupList reduceCounters; // eg reduce input bytes
  3: ThriftGroupList jobCounters; // eg task counts, etc
}

/** Unique task id */
struct ThriftTaskID {
  /** ID of the job to which the task belongs */
  1: ThriftJobID jobID

  /** What kind of task is this? */
  2: ThriftTaskType taskType

  /** Unique (to job) task id */
  3: i32 taskID

  /** Flattened to a unique string */
  4: string asString
}

/** Unique task attempt id */
struct ThriftTaskAttemptID {
  1: ThriftTaskID taskID
  2: i32 attemptID
  3: string asString
}

/** Describes the current state of a single attempt */
struct ThriftTaskStatus {
  1: ThriftTaskAttemptID taskID
  2: double progress
  3: ThriftTaskState state
  4: string diagnosticInfo
  5: string stateString
  6: string taskTracker

  7: i64 startTime
  8: i64 finishTime

  9: i64 outputSize

  10: ThriftTaskPhase phase

  11: ThriftGroupList counters

	12: i64 shuffleFinishTime,
	13: i64 sortFinishTime,
	14: i64 mapFinishTime,
}

/**
 * A ThriftTaskInProgress contains a list of
 * task attempts (speculatively executed instances of the same task).
 * These are indexed by TaskAttemptID.
 * For simplicity, we convert maps keyed on TaskAttemptIDs to maps keyed
 * on their string representation.
 *
 * Assumption: there won't be so many task attempts that retrieving a single task
 * will be too expensive.
 */
struct ThriftTaskInProgress {
  2: i64 execStartTime
  3: i64 execFinishTime
  4: double progress
  5: i64 startTime
  6: bool failed
  7: bool complete
  8: ThriftTaskID taskID
  9: list<ThriftTaskAttemptID> tasks
  /** TaskAttemptID (string) to ThriftTaskStatus map */
  10: map<string,ThriftTaskStatus> taskStatuses
  11: map<string,list<string>> taskDiagnosticData
  12: ThriftGroupList counters
  /* The last state reported (from TaskReport) */
  13: string mostRecentState
  /* The set of attempts that are currently running - could be empty. */
  14: list<string> runningAttempts
  /* The id of the successful attempt. If not complete, this field is meaningless */
  15: string successfulAttempt
}

/** TaskTracker status; contains details of individual tasks */
struct ThriftTaskTrackerStatus {
  1: string trackerName
  2: string host
  3: i32 httpPort
  4: i32 failureCount

  /** List of the state of all tasks on this tracker */
  5: list<ThriftTaskStatus> taskReports

  /** When did the JobTracker last hear from this TaskTracker? */
  6: i64 lastSeen

  /** Maximum possible number of both task types */
  7: i32 maxMapTasks
  8: i32 maxReduceTasks

  /** Main memory metrics, all in bytes */
  9: i64 totalVirtualMemory
  11: i64 totalPhysicalMemory
  13: i64 availableSpace

  /** Currently running and unassigned map and reduce tasks */
  14: i32 mapCount
  15: i32 reduceCount
}

/** Container structure for TaskTrackerStatus objects */
struct ThriftTaskTrackerStatusList {
  1: list<ThriftTaskTrackerStatus> trackers
}

/** States that the jobtracker may be in */
enum JobTrackerState {
  INITIALIZING,
  RUNNING
}

/** Enum version of the ints in JobStatus */
enum ThriftJobState {
		 RUNNING = 1,
		 SUCCEEDED = 2,
		 FAILED = 3,
		 PREP = 4,
		 KILLED = 5
}

/** Status of a job */
struct ThriftJobStatus {
  1: ThriftJobID jobID
  2: double mapProgress
  3: double reduceProgress
  4: double cleanupProgress
  5: double setupProgress
  6: ThriftJobState runState
  7: i64 startTime
  8: string user
  9: ThriftJobPriority priority
  10: string schedulingInfo
}

/** Job metadata */
struct ThriftJobProfile {
  1: string user
  2: ThriftJobID jobID
  3: string jobFile
  4: string name
  5: string queueName
}

/**
 * Container structure of a list of tasks. This list may have been put together
 * according to some selection criteria. That is, it may not correspond to the
 * mapTasks, or reduceTasks, etc. It may even contain tasks of different types.
 */
struct ThriftTaskInProgressList {
  /** A (possibly incomplete) list of tasks */
  1: list<ThriftTaskInProgress> tasks
  /** The total number of tasks in this full list. */
  2: i32 numTotalTasks
}

/** Status of *all* jobs, not just currently running ones */
struct ThriftJobInProgress {
  1: ThriftJobProfile profile
  2: ThriftJobStatus status
  3: ThriftJobID jobID
  4: i32 desiredMaps
  5: i32 desiredReduces
  6: i32 finishedMaps
  7: i32 finishedReduces
  8: ThriftJobPriority priority

  11: i64 startTime
  12: i64 finishTime
  13: i64 launchTime

  23: ThriftTaskInProgressList tasks
}

/** Container structure of a list of jobs, in case we ever want to add metadata */
struct ThriftJobList {
  1: list<ThriftJobInProgress> jobs
}

/** Container structure for job counts for a given user */
struct ThriftUserJobCounts {
  1: i32 nPrep,
  2: i32 nRunning,
  3: i32 nSucceeded,
  4: i32 nFailed,
  5: i32 nKilled
}

/** Status of the cluster as viewed by the jobtracker */
struct ThriftClusterStatus {
  1: i32 numActiveTrackers
  2: list<string> activeTrackerNames
  3: list<string> blacklistedTrackerNames
  4: i32 numBlacklistedTrackers
  5: i32 numExcludedNodes

  /* How often does the JobTracker check for expired tasks with the taskTracker */
  6: i64 taskTrackerExpiryInterval

  7: i32 mapTasks;
  8: i32 reduceTasks
  9: i32 maxMapTasks
  10: i32 maxReduceTasks
  11: JobTrackerState state

  /** Used and max memory for the cluster, in bytes */
  12: i64 usedMemory
  13: i64 maxMemory

  14: i32 totalSubmissions

  /* True if the JobTracker has restarted */
  15: bool hasRestarted

  /* True if the JobTracker has finished recovering after a restart */
  16: bool hasRecovered

  17: i64 startTime
  18: string hostname
  19: string identifier

	20: i32 httpPort
}

/** Merely an indicator that job wasn't found. */
exception JobNotFoundException {
}

/** Merely an indicator that task wasn't found. */
exception TaskNotFoundException {
}

/** Indicates that a task attempt wasn't found */
exception TaskAttemptNotFoundException {
}

/** Indicates that a tasktracker wasn't found */
exception TaskTrackerNotFoundException {
}

/** A proxy service onto a Jobtracker, exposing read-only methods for cluster monitoring */
service Jobtracker extends common.HadoopServiceBase {
	/** Get the name of the tracker exporting this service */
	string getJobTrackerName(10: common.RequestContext ctx),

	/** Get the current cluster status */
	ThriftClusterStatus getClusterStatus(10: common.RequestContext ctx),

	/** Get a list of job queues managed by this tracker */
	ThriftJobQueueList getQueues(10: common.RequestContext ctx)
				 	   throws(1: common.IOException err),

	/** Get a job by ID */
        ThriftJobInProgress getJob(10: common.RequestContext ctx, 1: ThriftJobID jobID)
                                  throws(1: JobNotFoundException err),

	/** Get a list of currently running jobs */
	ThriftJobList getRunningJobs(10: common.RequestContext ctx),

	/** Get a list of completed jobs */
	ThriftJobList getCompletedJobs(10: common.RequestContext ctx),

	/** Get a list of failed (due to error, not killed) jobs */
	ThriftJobList getFailedJobs(10: common.RequestContext ctx),

	/** Get a list of killed jobs */
	ThriftJobList getKilledJobs(10: common.RequestContext ctx),

	/** Get a list of all failed, completed and running jobs (could be expensive!) */
	ThriftJobList getAllJobs(10: common.RequestContext ctx),

        /** Get the count of jobs by status for a given user */
        ThriftUserJobCounts getUserJobCounts(1: common.RequestContext ctx, 2: string user),

        /** Get a (possibly incomplete) list of tasks */
        ThriftTaskInProgressList getTaskList(
                                      1: common.RequestContext ctx,
                                      2: ThriftJobID jobID,
                                      3: set<ThriftTaskType> types,
                                      4: set<ThriftTaskQueryState> states,
                                      5: string text,
                                      6: i32 count,
                                      7: i32 offset) throws(1: JobNotFoundException err),

        /** Get details of a task */
        ThriftTaskInProgress getTask(1: common.RequestContext ctx,
                                     2: ThriftTaskID taskID)
                        throws(1: JobNotFoundException jnf, 2: TaskNotFoundException tnf),

        /**
         * Get a list of groups of counters attached to the job with provided id.
         * This returns the total counters
         **/
        ThriftGroupList getJobCounters(10: common.RequestContext ctx,
                                        1: ThriftJobID jobID)
                                  throws(1: JobNotFoundException err),


        /** Return job counters rolled up by map, reduce, and total */
        ThriftJobCounterRollups getJobCounterRollups(10: common.RequestContext ctx,
                                                     1: ThriftJobID jobID)
                                  throws(1: JobNotFoundException err),


	/** Get all active trackers */
	ThriftTaskTrackerStatusList getActiveTrackers(10: common.RequestContext ctx),

	/** Get all blacklisted trackers */
	ThriftTaskTrackerStatusList getBlacklistedTrackers(10: common.RequestContext ctx),

	/** Get all trackers */
	ThriftTaskTrackerStatusList getAllTrackers(10: common.RequestContext ctx),

	/** Get a single task tracker by name */
	ThriftTaskTrackerStatus getTracker(10: common.RequestContext ctx, 1: string name)
	          throws(1: TaskTrackerNotFoundException tne),

  /** Get the current time in ms according to the JT */
  i64 getCurrentTime(10: common.RequestContext ctx),

  /** Get the xml for a job's configuration, serialised from the local filesystem on the JT */
  string getJobConfXML(10: common.RequestContext ctx, 1: ThriftJobID jobID)
            throws(1: common.IOException err),

  /** Kill a job */
  void killJob(10: common.RequestContext ctx, 1: ThriftJobID jobID)
                             throws(1: common.IOException err, 2: JobNotFoundException jne),

  /** Kill a task attempt */
  void killTaskAttempt(10: common.RequestContext ctx, 1: ThriftTaskAttemptID attemptID)
                                   throws(1: common.IOException err,
                                          2: TaskAttemptNotFoundException tne,
                                          3: JobNotFoundException jne),

  /** Set a job's priority */
  void setJobPriority(10: common.RequestContext ctx,
                      1: ThriftJobID jobID,
                      2: ThriftJobPriority priority)
      throws(1: common.IOException err, 2: JobNotFoundException jne),

  /** Get an MR delegation token. */
  common.ThriftDelegationToken getDelegationToken(10:common.RequestContext ctx, 1:string renewer) throws(1: common.IOException err)
}

