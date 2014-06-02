/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openflamingo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Status Code Resolver.
 *
 * @author Edward KIM
 * @since 0.2
 */
public class HttpStatusCodeResolver {

    private static Map codeMap = new HashMap(54);

    static {
        codeMap.put("100", "Continue");
        codeMap.put("101", "Switching Protocols");
        codeMap.put("102", "Processing (WebDAV) (RFC 2518 )");
        codeMap.put("200", "OK");
        codeMap.put("201", "Created");
        codeMap.put("202", "Accepted");
        codeMap.put("203", "Non-Authoritative Information (since HTTP/1.1)");
        codeMap.put("204", "No Content");
        codeMap.put("205", "Reset Content");
        codeMap.put("206", "Partial Content");
        codeMap.put("207", "Multi-Status (WebDAV)");
        codeMap.put("300", "Multiple Choices");
        codeMap.put("301", "Moved Permanently");
        codeMap.put("302", "Found");
        codeMap.put("303", "See Other (since HTTP/1.1)");
        codeMap.put("304", "Not Modified");
        codeMap.put("305", "Use Proxy (since HTTP/1.1)");
        codeMap.put("306", "Switch Proxy");
        codeMap.put("307", "Temporary Redirect (since HTTP/1.1)");
        codeMap.put("400", "Bad Request");
        codeMap.put("401", "Unauthorized");
        codeMap.put("402", "Payment Required");
        codeMap.put("403", "Forbidden");
        codeMap.put("404", "Not Found");
        codeMap.put("405", "Method Not Allowed");
        codeMap.put("406", "Not Acceptable");
        codeMap.put("407", "Proxy Authentication Required");
        codeMap.put("408", "Request Timeout");
        codeMap.put("409", "Conflict");
        codeMap.put("410", "Gone");
        codeMap.put("411", "Length Required");
        codeMap.put("412", "Precondition Failed");
        codeMap.put("413", "Request Entity Too Large");
        codeMap.put("414", "Request-URI Too Long");
        codeMap.put("415", "Unsupported Media Type");
        codeMap.put("416", "Requested Range Not Satisfiable");
        codeMap.put("417", "Expectation Failed");
        codeMap.put("418", "I'm a teapot");
        codeMap.put("422", "Unprocessable Entity (WebDAV) (RFC 4918 )");
        codeMap.put("423", "Locked (WebDAV) (RFC 4918 )");
        codeMap.put("424", "Failed Dependency (WebDAV) (RFC 4918 )");
        codeMap.put("425", "Unordered Collection");
        codeMap.put("426", "Upgrade Required (RFC 2817 )");
        codeMap.put("449", "Retry With");
        codeMap.put("500", "Internal Server Error");
        codeMap.put("501", "Not Implemented");
        codeMap.put("502", "Bad Gateway");
        codeMap.put("503", "Service Unavailable");
        codeMap.put("504", "Gateway Timeout");
        codeMap.put("505", "HTTP Version Not Supported");
        codeMap.put("506", "Variant Also Negotiates (RFC 2295 )");
        codeMap.put("507", "Insufficient Storage (WebDAV) (RFC 4918 )");
        codeMap.put("509", "Bandwidth Limit Exceeded (Apache bw/limited extension)");
        codeMap.put("510", "Not Extended (RFC 2774 )");
    }

    /**
     * HTTP Procotol의 Status Code에 따른 메시지를 해석한다.
     *
     * @param statusCode HTTP Protocol Status Code
     * @return HTTP Status Code의 메시지
     */
    public static String resolve(int statusCode) {
        return (String) codeMap.get(String.valueOf(statusCode));
    }
}