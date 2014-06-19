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
 * Common declarations for Hadoop Thrift interfaces
 */

/*
 * Namespaces for generated code. The idea is to keep code generated by
 * Thrift under a 'hadoop.api' namespace, so that a higher-level set of
 * functions and classes may be defined under 'hadoop'.
 */

namespace cpp     hadoop.api
namespace csharp  Hadoop.API
namespace java    org.apache.hadoop.thriftfs.api
namespace perl    Hadoop.API
namespace php     hadoop_api
namespace py      hadoop.api.common
namespace rb      Hadoop.API

/** Generic I/O error */
exception IOException {
  /** Error message. */
  1: string msg,

  /** Textual representation of the call stack. */
  2: string stack

  /** The Java class of the Exception (may be a subclass) */
  3: string clazz
}

/**
 * Information about the compilation version of this server
 */
struct VersionInfo {
  1: string version
  2: string revision
  4: string compileDate
  5: string compilingUser
  6: string url
  7: string buildVersion
}


/** A single stack frame in a stack dump */
struct StackTraceElement {
  1: string className
  2: string fileName
  3: i32 lineNumber
  4: string methodName
  5: bool isNativeMethod
  6: string stringRepresentation
}

/** Info about a thread with its corresponding stack trace */
struct ThreadStackTrace {
  1: string threadName
  2: string threadStringRepresentation
  3: bool isDaemon

  4: list<StackTraceElement> stackTrace;
}

/**
 * Memory available via java.lang.Runtime
 */
struct RuntimeInfo {
  1:i64 totalMemory
  2:i64 freeMemory
  3:i64 maxMemory
}

/**
 * Context options for every request.
 */
struct RequestContext {
  /**
   * This map turns into a Configuration object in the server and
   * is currently used to construct a UserGroupInformation to
   * authenticate this request.
   */
  1:map<string, string> confOptions
}

struct MetricsRecord {
  2: map<string, string> tags
  3: map<string, i64> metrics
}

struct MetricsContext {
  1: string name
  2: bool isMonitoring
  3: i32 period

  4: map<string, list<MetricsRecord>> records
}

struct ThriftDelegationToken {
  1: binary delegationTokenBytes
}

service HadoopServiceBase {
  /** Return the version information for this server */
  VersionInfo getVersionInfo(10:RequestContext ctx);
  RuntimeInfo getRuntimeInfo(10:RequestContext ctx);
  list<ThreadStackTrace> getThreadDump(10:RequestContext ctx);
  list<MetricsContext> getAllMetrics(10:RequestContext ctx)
    throws (1:IOException err);
  MetricsContext getMetricsContext(10:RequestContext ctx, 1:string contextName)
    throws (1:IOException err);
}
