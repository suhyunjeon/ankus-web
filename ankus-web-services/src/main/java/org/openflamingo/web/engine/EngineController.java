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
package org.openflamingo.web.engine;

import org.openflamingo.model.rest.Engine;
import org.openflamingo.model.rest.FileInfo;
import org.openflamingo.model.rest.HiveServer;
import org.openflamingo.model.rest.Response;
import org.openflamingo.provider.engine.AdminService;
import org.openflamingo.provider.engine.WorkflowEngineService;
import org.openflamingo.web.admin.HadoopClusterAdminService;
import org.openflamingo.web.admin.HiveAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.helpers.MessageFormatter.format;

@Controller
@RequestMapping("/admin/engine")
public class EngineController {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(EngineController.class);

    @Autowired
    private EngineService engineService;

    @Autowired
    private HadoopClusterAdminService hadoopClusterAdminService;

    @Autowired
    private HiveAdminService hiveAdminService;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "regist", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response registCollectionJob(@RequestParam Long serverId, @RequestBody String xml) {
        Response response = new Response();
        if (serverId == null || StringUtils.isEmpty(xml)) {
            response.setSuccess(false);
            return response;
        }

        logger.debug("로그 수집 요청을 처리합니다. 처리할 로그 수집 요청은 다음과 같습니다.\n{}", xml);

        try {
            Engine engine = engineService.getEngine(serverId);
            WorkflowEngineService workflowEngineService = getRemoteWorkflowEngineService(engine);
            response.setSuccess(workflowEngineService.registCollectionJob(xml));
        } catch (Exception ex) {
            String message = "Log Collector에 로그 수집 요청을 할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "engines", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getEngines(@RequestParam(defaultValue = "ALL") String type) {
        Response response = new Response();

        try {
            response.setSuccess(true);
            List<Engine> engines = engineService.getEngines();
            if (engines != null) {
                List<Engine> filters = new ArrayList<Engine>();
                for (Engine engine : engines) {
                    if ("HADOOP".equals(type) && engine.getHadoopClusterId() > 0) {
                        filters.add(engine);
                    } else if ("HIVE".equals(type) && engine.getHiveServerId() > 0) {
                        filters.add(engine);
                    } else if ("ALL".equals(type)) {
                        filters.add(engine);
                    }
                }

                for (Engine engine : filters) {
                    Map param = new HashMap();

                    param.put("id", engine.getId());
                    param.put("instanceName", engine.getName());
                    param.put("serverUrl", getWorkflowEngineUrl(engine));
                    param.put("ip", engine.getIp());
                    param.put("port", engine.getPort());

                    if (("ALL".equals(type) || "HADOOP".equals(type))) {
                        param.put("hadoopClusterId", engine.getHadoopClusterId());
                        param.put("hadoopClusterName", hadoopClusterAdminService.getHadoopCluster(engine.getHadoopClusterId()).getName());
                    }

//                    HiveServer hiveServer = hiveAdminService.getHiveServer(engine.getHiveServerId());
//                    if ("ALL".equals(type) || ("HIVE".equals(type) && hiveServer != null)) {
//                        param.put("hiveServerId", engine.getHiveServerId());
//                        param.put("hiveServerName", hiveServer.getName());
//                    }

                    try {
                        WorkflowEngineService workflowEngineService = getRemoteWorkflowEngineService(engine);
                        Map status = workflowEngineService.getStatus();

                        logger.info("Workflow Engine({})의 상태 정보: {}", getWorkflowEngineUrl(engine), status);

                        param.put("schedulerName", status.get("schedulerName"));
                        param.put("schedulerId", status.get("schedulerId"));
                        param.put("hostAddress", status.get("hostAddress"));
                        param.put("hostName", status.get("hostName"));
                        param.put("runningJob", status.get("runningJob"));

                        param.put("status", "RUNNING");
                    } catch (Exception ex) {
                        param.put("status", "FAIL");
                    }
                    response.getList().add(param);
                }
                response.setTotal(response.getList().size());
            } else {
                response.setTotal(0);
            }
        } catch (Exception ex) {
            String msg = "Workflow Engine 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(msg);
            response.getError().setCause(ex.getMessage());

            logger.warn(msg, ex);
        }
        return response;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response delete(@RequestBody Engine engine) {
        Response response = new Response();
        try {
            response.setSuccess(engineService.removeEngine(engine.getId()));
        } catch (Exception ex) {
            String message = "Workflow Engine를 삭제할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "envs", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getEnvs(@RequestParam(defaultValue = "") String serverUrl,
                            @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();

        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            WorkflowEngineService workflowEngineService = this.getRemoteWorkflowEngineService(serverUrl);
            List<Map> systemProperties = workflowEngineService.getEnvironments();
            response.getList().addAll(systemProperties);
        } catch (Exception ex) {
            String message = "Workflow Engine의 환경 변수 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "props", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getPros(@RequestParam(defaultValue = "") String serverUrl,
                            @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            WorkflowEngineService workflowEngineService = this.getRemoteWorkflowEngineService(serverUrl);
            List<Map> systemProperties = workflowEngineService.getSystemProperties();
            response.getList().addAll(systemProperties);
        } catch (Exception ex) {
            String message = "Workflow Engine의 시스템 속성 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "triggers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getTriggers(@RequestParam(defaultValue = "") String serverUrl,
                                @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            WorkflowEngineService workflowEngineService = this.getRemoteWorkflowEngineService(serverUrl);
            List<Map> systemProperties = workflowEngineService.getTriggers();
            response.getList().addAll(systemProperties);
        } catch (Exception ex) {
            String message = "Workflow Engine의 시스템 속성 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "running", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getRunningJobs(@RequestParam(defaultValue = "") String serverUrl,
                                   @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            WorkflowEngineService workflowEngineService = this.getRemoteWorkflowEngineService(serverUrl);
            List<Map> systemProperties = workflowEngineService.getRunningJob();
            response.getList().addAll(systemProperties);
        } catch (Exception ex) {
            String message = "Workflow Engine의 실행중인 작업 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "job", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getJob(@RequestParam(defaultValue = "") String serverUrl,
                           @RequestParam(defaultValue = "") String jobName,
                           @RequestParam(defaultValue = "") String jobGroup,
                           @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            WorkflowEngineService workflowEngineService = this.getRemoteWorkflowEngineService(serverUrl);
            List<Map> jobInfos = workflowEngineService.getJobInfos(jobName, jobGroup);
            response.getList().addAll(jobInfos);
        } catch (Exception ex) {
            String message = "Workflow Engine의 실행중인 작업 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response add(@RequestBody Engine engine) {
        logger.debug("Workflow Engine을 추가합니다. 추가할 정보: {}", engine);

        Response response = new Response();
        try {
            response.setSuccess(engineService.addEngine(engine));
            response.setObject(engine);
        } catch (Exception ex) {
            String message = "Workflow Engine을 추가할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getHistories(@RequestParam(defaultValue = "") String serverUrl,
                                 @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            response.setSuccess(true);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(this.getJsonMediaType());

            ResponseEntity<Response> responseEntity = restTemplate.exchange(StringUtils.replace(serverUrl, "status", "histories")
                    + "?start=" + start + "&page=" + page + "&limit=" + limit,
                    HttpMethod.GET,
                    new HttpEntity<Response>(headers),
                    Response.class);

            response.getList().addAll(responseEntity.getBody().getList());
            response.setTotal(responseEntity.getBody().getTotal());
        } catch (Exception ex) {
            String message = "Workflow Engine의 시스템 속성 정보를 확인할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "directory", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getDirectory(@RequestParam(defaultValue = "") String serverUrl,
                                 @RequestParam String node) {
        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(this.getJsonMediaType());

            MultiValueMap param = new LinkedMultiValueMap();
            param.add("node", node);

            HttpEntity<?> httpEntity = new HttpEntity<Object>(param, headers);

            System.out.println("------------serverUrl-------------");
            System.out.println("serverUrl : " + serverUrl);

            ResponseEntity<Response> responseEntity = restTemplate.exchange(StringUtils.replace(serverUrl, "status", "directory"),
                    HttpMethod.POST, httpEntity, Response.class);

            response.getList().addAll(responseEntity.getBody().getList());
            response.setTotal(responseEntity.getBody().getTotal());
            response.setSuccess(true);
        } catch (Exception ex) {
            String message = " 요청한 경로(" + node + ")에 접근할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "aio", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getDirectoryAndFile(@RequestParam(defaultValue = "0") Long engineId,
                                        @RequestParam(defaultValue = "/") String node) {

        Response response = new Response();
        if (engineId == 0) {
            response.setSuccess(true);
            return response;
        }

        try {
            Engine engine = engineService.getEngine(engineId);
            AdminService adminService = getAdminService(getAdminServiceUrl(engine));
            List<FileInfo> fileinfo = adminService.getDirectoryAndFiles(node);

            response.getList().addAll(fileinfo);
            response.setTotal(fileinfo.size());
            response.setSuccess(true);
        } catch (Exception ex) {
            String message = " 요청한 경로(" + node + ")에 접근할 수 없습니다.";
            response.setSuccess(false);
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    @RequestMapping(value = "file", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getFiles(
            @RequestParam(defaultValue = "") String serverUrl, @RequestParam String node,
            @RequestParam int start, @RequestParam int page, @RequestParam int limit) {

        Response response = new Response();
        if (StringUtils.isEmpty(serverUrl)) {
            response.setSuccess(true);
            return response;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(this.getJsonMediaType());

            MultiValueMap param = new LinkedMultiValueMap();
            param.add("node", node);

            HttpEntity<?> httpEntity = new HttpEntity<Object>(param, headers);

            ResponseEntity<Response> responseEntity = restTemplate.exchange(StringUtils.replace(serverUrl, "status", "file"),
                    HttpMethod.POST, httpEntity, Response.class);

            response.getList().addAll(responseEntity.getBody().getList());
            response.setTotal(responseEntity.getBody().getTotal());
            response.setSuccess(true);
        } catch (Exception ex) {
            response.setSuccess(false);
            String message = " 요청한 경로(" + node + ")에 접근할 수 없습니다.";
            response.getError().setMessage(message);
            response.getError().setCause(ex.getMessage());

            logger.warn(message, ex);
        }
        return response;
    }

    private List<MediaType> getJsonMediaType() {
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        return mediaTypes;
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @return Remote Workflow Engine Service
     */
    private WorkflowEngineService getRemoteWorkflowEngineService(String url) {
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl(url);
        factoryBean.setServiceInterface(WorkflowEngineService.class);
        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setConnectTimeout(3000);
        factoryBean.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        factoryBean.afterPropertiesSet();
        return (WorkflowEngineService) factoryBean.getObject();
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @return Remote Workflow Engine Service
     */
    private WorkflowEngineService getRemoteWorkflowEngineService(Engine engine) {
        return getRemoteWorkflowEngineService(getWorkflowEngineUrl(engine));
    }

    /**
     * Remote Workflow Engine URL을 구성한다.
     *
     * @param engine Workflow Engine
     * @return Remote Workflow Engine의 URL
     */
    private String getWorkflowEngineUrl(Engine engine) {
        return format("http://{}:{}/remote/engine", engine.getIp(), engine.getPort()).getMessage();
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @param ip   Workflow Engine의 IP
     * @param port Workflow Engine의 Port
     * @return Remote Workflow Engine Service
     */
    private AdminService getHistoryService(String ip, String port) {
        Engine engine = new Engine();
        engine.setIp(ip);
        engine.setPort(port);
        return getAdminService(getAdminServiceUrl(engine));
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @return Remote Workflow Engine Service
     */
    private AdminService getAdminService(String url) {
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl(url);
        factoryBean.setServiceInterface(AdminService.class);
        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();
        factoryBean.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        factoryBean.afterPropertiesSet();
        return (AdminService) factoryBean.getObject();
    }

    /**
     * Remote Workflow Engine URL을 구성한다.
     *
     * @param engine Workflow Engine
     * @return Remote Workflow Engine의 URL
     */
    private String getAdminServiceUrl(Engine engine) {
        return format("http://{}:{}/remote/admin", engine.getIp(), engine.getPort()).getMessage();
    }
}