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
package org.openflamingo.web.fs;

import org.codehaus.jackson.map.ObjectMapper;
import org.openflamingo.fs.hdfs.HdfsFileInfo;
import org.openflamingo.model.rest.*;
import org.openflamingo.provider.fs.FileSystemService;
import org.openflamingo.util.ExceptionUtils;
import org.openflamingo.util.FileUtils;
import org.openflamingo.util.StringUtils;
import org.openflamingo.web.admin.HadoopClusterAdminService;
import org.openflamingo.web.configuration.ConfigurationManager;
import org.openflamingo.web.core.LocaleSupport;
import org.openflamingo.web.core.RemoteService;
import org.openflamingo.web.engine.EngineService;
import org.openflamingo.web.security.SessionUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HDFS Browser REST Controller.
 *
 * @author Edward KIM
 * @since 0.3
 */
@Controller
@RequestMapping("/fs/hdfs")
public class HdfsBrowserController extends LocaleSupport {

    /**
     * HDFS Folder Separator
     */
    private static final String FILE_SEPARATOR = "/";

    /**
     * Parameter Encoding Option
     */
    private static final String CHARSET = "UTF-8";

    /**
     * Hadoop Cluster 정보를 얻기 위한 Hadoop Cluster Admin Service.
     */
    @Autowired
    private HadoopClusterAdminService hadoopClusterAdminService;

    /**
     * Workflow Engine 정보를 얻기 위한 Engine Service.
     */
    @Autowired
    private EngineService engineService;

    /**
     * Remote Service Lookup Service.
     */
    @Autowired
    private RemoteService lookupService;

    /**
     * 기본 금지 경로 목록
     */
    public static String DEFAULT_FORBIDDEN_PATH = "/tmp,/tmp/**/*,/user,/user/hive/**/*";

    /**
     * 지정한 경로의 디렉토리 목록을 획득한다.
     *
     * @param node     디렉토리 목록을 획득한 경로(ExtJS에서 사용하는 파라미터)
     * @param engineId 파일 시스템에 접근하기 위해서 필요한 워크플로우 엔진
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "directory", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getDirectories(@RequestParam String node,
                                   @RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();

        if (StringUtils.isEmpty(node) || StringUtils.isEmpty(engineId)) {
            response.setSuccess(true);
            return response;
        }

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", node);
            command.putBoolean("directoryOnly", true);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            List<FileInfo> directories = fileSystemService.getDirectories(getContext(engine), command);
            response.getList().addAll(directories);
            response.setTotal(directories.size());
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리를 생성한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(path, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "newDirectory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response createDirectory(@RequestBody Map<String, String> params) {
        Response response = new Response();

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", params.get("path"));

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            boolean directory = fileSystemService.createDirectory(context, command);
            response.setSuccess(directory);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리를 삭제한다.
     *
     * @param params 디렉토리를 삭제하는 경로 및 Engine
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "deleteDirectory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response deleteDirectory(@RequestBody Map<String, String> params) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", params.get("path"));

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            boolean directory = fileSystemService.deleteDirectory(context, command);
            response.setSuccess(directory);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리명을 변경한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(from, to, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "renameDirectory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response renameDirectory(@RequestBody Map<String, String> params) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("from", params.get("from"));
            command.putString("to", params.get("to"));

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            boolean directory = fileSystemService.renameDirectory(context, command);
            response.setSuccess(directory);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리를 이동한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(from, to, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "moveDirectory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response moveDirectory(@RequestBody Map<String, String> params) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("from", params.get("from"));
            command.putString("to", params.get("to"));

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            boolean directory = fileSystemService.moveDirectory(context, command);
            response.setSuccess(directory);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리를 복사한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(from, to, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "copyDirectory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response copyDirectory(@RequestBody Map<String, String> params) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("from", params.get("from"));
            command.putString("to", params.get("to"));

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            boolean directory = fileSystemService.copyDirectory(context, command);
            response.setSuccess(directory);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 디렉토리의 정보를 확인한다.
     *
     * @param path 정보를 확인할 디렉토리 경로
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "infoDirectory", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response infoDirectory(@RequestParam String path,
                                  @RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            FileInfo fileInfo = fileSystemService.getFileInfo(getContext(engine), command);

            Map<String, Object> map = new HashMap();
            // Basic
            map.put("name", fileInfo.getFilename());
            map.put("path", fileInfo.getFullyQualifiedPath());
            map.put("length", fileInfo.getLength());
            map.put("modification", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(fileInfo.getModificationTime())));
            map.put("isFile", fileInfo.isFile());
            map.put("isDirectory", fileInfo.isDirectory());

            // Permission
            map.put("ownerRead", fileInfo.getPermission().charAt(0) != '-' ? true : false);
            map.put("ownerWrite", fileInfo.getPermission().charAt(1) != '-' ? true : false);
            map.put("ownerExecute", fileInfo.getPermission().charAt(2) != '-' ? true : false);
            map.put("groupRead", fileInfo.getPermission().charAt(3) != '-' ? true : false);
            map.put("groupWrite", fileInfo.getPermission().charAt(4) != '-' ? true : false);
            map.put("groupExecute", fileInfo.getPermission().charAt(5) != '-' ? true : false);
            map.put("otherRead", fileInfo.getPermission().charAt(6) != '-' ? true : false);
            map.put("otherWrite", fileInfo.getPermission().charAt(7) != '-' ? true : false);
            map.put("otherExecute", fileInfo.getPermission().charAt(8) != '-' ? true : false);

            // Space
            map.put("blockSize", fileInfo.getBlockSize());
            map.put("replication", fileInfo.getReplication());
            map.put("directoryCount", fileInfo.getReplication());
            map.put("fileCount", ((HdfsFileInfo) fileInfo).getFileCount());
            map.put("quota", ((HdfsFileInfo) fileInfo).getQuota());
            map.put("spaceConsumed", ((HdfsFileInfo) fileInfo).getSpaceConsumed());
            map.put("spaceQuota", ((HdfsFileInfo) fileInfo).getSpaceQuota());

            response.getMap().putAll(map);
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 경로의 파일 목록을 획득한다.
     *
     * @param path 정보를 획득할 경로
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "file", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getFiles(@RequestParam(defaultValue = "") String engineId,
                             @RequestParam(defaultValue = "/") String path) {
        Response response = new Response();
        if (StringUtils.isEmpty(engineId)) {
            response.setSuccess(true);
            return response;
        }

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            List<FileInfo> files = fileSystemService.getFiles(getContext(engine), command);
            response.getList().addAll(files);
            response.setTotal(files.size());
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 파일의 이름을 변경한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(path, filename, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "rename", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response renameFile(@RequestBody Map<String, String> params) {
        String path = params.get("path");
        String filename = params.get("filename");

        Response response = new Response();

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);
            command.putString("filename", filename);

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            boolean renamed = fileSystemService.renameFile(context, command);
            response.setSuccess(renamed);
            response.getMap().put("path", FileUtils.getPath(path));
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 경로의 파일을 복사한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(paths, to, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "copy", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response copyFiles(@RequestBody Map<String, String> params) {
        String paths = params.get("paths");
        String to = params.get("to");

        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            String[] fromItems = paths.split(",");
            List<String> files = new java.util.ArrayList<String>();
            for (String item : fromItems) {
                files.add(item);
            }
            command.putObject("from", files);
            command.putString("to", to);

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            List<String> copied = fileSystemService.copyFiles(context, command);
            response.setSuccess(true);
            response.getMap().put("path", FileUtils.getPath(fromItems[0]));
            response.getList().addAll(copied);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 경로의 파일을 이동한다.
     *
     * @param params 클라이언트에서 전송한 파라마터(paths, to, engineId)
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "move", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response moveFiles(@RequestBody Map<String, String> params) {
        String paths = params.get("paths");
        String to = params.get("to");

        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            String[] fromItems = paths.split(",");
            List<String> files = new java.util.ArrayList<String>();
            for (String item : fromItems) {
                files.add(item);
            }
            command.putObject("from", files);
            command.putString("to", to);

            Engine engine = engineService.getEngine(Long.parseLong(params.get("engineId")));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            List<String> copied = fileSystemService.moveFiles(context, command);
            response.setSuccess(true);
            response.getMap().put("path", FileUtils.getPath(fromItems[0]));
            response.getList().addAll(copied);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    @RequestMapping(value = "deleteFiles", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response deleteFiles(@RequestParam String engineId,
                                @RequestBody String[] paths) throws IOException {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putObject("path", Arrays.asList(paths));

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            List<String> notDeleted = fileSystemService.deleteFiles(context, command);
            response.setSuccess(true);
            response.getList().addAll(notDeleted);
            response.getMap().put("path", FileUtils.getPath(paths[0]));
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 경로의 파일을 삭제한다.
     *
     * @param path 삭제할 경로
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "deleteFile", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response deleteFiles(@RequestParam List<String> path,
                                @RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putObject("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            List<String> notDeleted = fileSystemService.deleteFiles(context, command);
            response.setSuccess(true);
            response.getList().addAll(notDeleted);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 경로의 파일 정보를 획득한다.
     *
     * @param path 정보를 획득할 경로
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "info", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getInfo(@RequestParam String path,
                            @RequestParam(defaultValue = "") String engineId) {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            HdfsFileInfo fileInfo = (HdfsFileInfo) fileSystemService.infoFile(getContext(engine), command);

            Map<String, Object> map = new HashMap();
            // Basic
            map.put("name", fileInfo.getFilename());
            map.put("path", fileInfo.getFullyQualifiedPath());
            map.put("length", fileInfo.getLength());
            map.put("modification", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(fileInfo.getModificationTime())));
            map.put("isFile", fileInfo.isFile());
            map.put("isDirectory", fileInfo.isDirectory());

            // Permission
            map.put("ownerRead", fileInfo.getPermission().charAt(0) != '-' ? true : false);
            map.put("ownerWrite", fileInfo.getPermission().charAt(1) != '-' ? true : false);
            map.put("ownerExecute", fileInfo.getPermission().charAt(2) != '-' ? true : false);
            map.put("groupRead", fileInfo.getPermission().charAt(3) != '-' ? true : false);
            map.put("groupWrite", fileInfo.getPermission().charAt(4) != '-' ? true : false);
            map.put("groupExecute", fileInfo.getPermission().charAt(5) != '-' ? true : false);
            map.put("otherRead", fileInfo.getPermission().charAt(6) != '-' ? true : false);
            map.put("otherWrite", fileInfo.getPermission().charAt(7) != '-' ? true : false);
            map.put("otherExecute", fileInfo.getPermission().charAt(8) != '-' ? true : false);

            // Space
            map.put("blockSize", fileInfo.getBlockSize());
            map.put("replication", fileInfo.getReplication());
            map.put("directoryCount", fileInfo.getDirectoryCount());
            map.put("fileCount", fileInfo.getFileCount());
            map.put("quota", fileInfo.getQuota());
            map.put("spaceConsumed", fileInfo.getSpaceConsumed());
            map.put("spaceQuota", fileInfo.getSpaceQuota());

            response.getMap().putAll(map);
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 파일을 업로드한다.
     *
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "upload", method = RequestMethod.POST,
            consumes = {"multipart/form-data"}
    )
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> upload(HttpServletRequest req) throws IOException {
        Response response = new Response();
        if (!(req instanceof DefaultMultipartHttpServletRequest)) {
            response.setSuccess(false);
            response.getError().setCause(message("S_FS_SERVICE", "CANNOT_UPLOAD_INVALID", null));
            response.getError().setMessage(message("S_FS_SERVICE", "CANNOT_UPLOAD", null));
            String json = new ObjectMapper().writeValueAsString(response);
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        InputStream inputStream = null;
        try {
            DefaultMultipartHttpServletRequest request = (DefaultMultipartHttpServletRequest) req;
            String pathToUpload = request.getParameter("path");
            String engineId = req.getParameter("engineId");
            MultipartFile uploadedFile = request.getFile("file");
            String originalFilename = uploadedFile.getOriginalFilename();
            String fullyQualifiedPath = pathToUpload.equals("/") ? pathToUpload + originalFilename : pathToUpload + FILE_SEPARATOR + originalFilename;
            inputStream = uploadedFile.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);

            Context context = getContext(engine);
            String forbiddenPaths = ConfigurationManager.getConfigurationManager().get("hdfs.delete.forbidden.paths", DEFAULT_FORBIDDEN_PATH);
            context.putString("hdfs.delete.forbidden.paths", forbiddenPaths);

            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", fullyQualifiedPath);
            command.putObject("content", bytes);

            boolean file = fileSystemService.save(context, command);
            response.setSuccess(file);
            response.getMap().put("directory", pathToUpload);

            String json = new ObjectMapper().writeValueAsString(response);
            return new ResponseEntity(json, HttpStatus.OK);
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));

            String json = new ObjectMapper().writeValueAsString(response);
            return new ResponseEntity(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 파일을 다운로드한다.
     *
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "download", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity download(HttpServletResponse res, @RequestParam String path,
                                   @RequestParam(defaultValue = "") String engineId) {
        HttpHeaders headers = new HttpHeaders();
        if (org.apache.commons.lang.StringUtils.isEmpty(path)) {
            headers.set("message", message("S_FS_SERVICE", "INVALID_PARAMETER", null));
            return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
        }

        String filename = FileUtils.getFilename(path);

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putObject("path", path);
            command.putObject("filename", filename);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            byte[] bytes = fileSystemService.load(getContext(engine), command);

            res.setHeader("Content-Length", "" + bytes.length);
            res.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            res.setHeader("Content-Disposition", MessageFormatter.format("form-data; name={}; filename={}", URLEncoder.encode(path, CHARSET), URLEncoder.encode(filename, CHARSET)).getMessage());
            res.setStatus(200);
            FileCopyUtils.copy(bytes, res.getOutputStream());
            res.flushBuffer();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception ex) {
            headers.set("message", ex.getMessage());
            if (ex.getCause() != null) headers.set("cause", ex.getCause().getMessage());
            return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 파일 시스템의 상태 정보를 확인한다.
     *
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "fileSystemStatus", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getFileSystemStatus(@RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();

        if (StringUtils.isEmpty(engineId)) {
            response.setSuccess(true);
            return response;
        }

        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("status", "hdfs");

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            Map<String, Object> param = fileSystemService.getFileSystemStatus(getContext(engine), command);
            response.getMap().putAll(param);
            response.setTotal(param.size());
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 디렉토리의 파일 용량를 확인한다.
     *
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "size", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getSize(@RequestParam String path,
                            @RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            int size = fileSystemService.getSize(getContext(engine), command);
            Map<String, Object> map = new HashMap();
            map.put("size", size);
            response.setMap(map);
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    /**
     * 지정한 디렉토리의 파일 갯수를 확인한다.
     *
     * @return REST Response JAXB Object
     */
    @RequestMapping(value = "count", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getCount(@RequestParam String path,
                             @RequestParam(defaultValue = "") String engineId) {
        Response response = new Response();
        try {
            FileSystemCommand command = new FileSystemCommand();
            command.putString("path", path);

            Engine engine = engineService.getEngine(Long.parseLong(engineId));
            FileSystemService fileSystemService = (FileSystemService) lookupService.getService(RemoteService.HDFS, engine);
            int count = fileSystemService.getCount(getContext(engine), command);
            Map<String, Object> map = new HashMap();
            map.put("count", count);
            response.setMap(map);
            response.setSuccess(true);
            return response;
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            return response;
        }
    }

    private Context getContext(Engine engine) {
        HadoopCluster hadoopCluster = hadoopClusterAdminService.getHadoopCluster(engine.getHadoopClusterId());
        Context context = new Context();
        context.putObject(Context.AUTORITY, new Authority(SessionUtils.getUsername(), SecurityLevel.SUPER));
        context.putObject(Context.HADOOP_CLUSTER, new HadoopCluster(hadoopCluster.getHdfsUrl()));
        context.putString("username", SessionUtils.getUsername());
        return context;
    }
}