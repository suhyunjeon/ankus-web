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
package org.openflamingo.web.dashboard;

import org.openflamingo.model.rest.ActionHistory;
import org.openflamingo.model.rest.Engine;
import org.openflamingo.model.rest.Response;
import org.openflamingo.model.rest.WorkflowHistory;
import org.openflamingo.provider.engine.HistoryService;
import org.openflamingo.util.ExceptionUtils;
import org.openflamingo.util.StringUtils;
import org.openflamingo.web.core.LocaleSupport;
import org.openflamingo.web.engine.EngineService;
import org.openflamingo.web.security.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.slf4j.helpers.MessageFormatter.format;

/**
 * Dashboard REST Controller.
 *
 * @author Edward KIM
 * @since 0.4
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController extends LocaleSupport {

    /**
     * Flamingo Engine Management Remote Service
     */
    @Autowired
    private EngineService engineService;

    /**
     * 지정한 조건의 워크플로우 실행 이력을 조회한다.
     *
     * @param startDate    시작날짜
     * @param endDate      종료 날짜
     * @param workflowName 워크플로우명
     * @param jobType      Job 유형
     * @param status       상태코드
     * @param sort         정렬할 컬럼명
     * @param dir          정렬 방식(ASC, DESC)
     * @param start        시작 페이지
     * @param limit        페이지당 건수
     * @return 워크플로우 실행 이력 목록
     */
    @RequestMapping(value = "workflows", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getWorkflows(@RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate,
                                 @RequestParam(defaultValue = "") String workflowName,
                                 @RequestParam(defaultValue = "WORKFLOW") String jobType,
                                 @RequestParam(defaultValue = "0") long engineId,
                                 @RequestParam(defaultValue = "ALL") String status,
                                 @RequestParam(defaultValue = "ID") String sort,
                                 @RequestParam(defaultValue = "DESC") String dir,
                                 @RequestParam(defaultValue = "0") int start, @RequestParam(defaultValue = "16") int limit) {

        Response response = new Response();
        try {
            Engine engine = engineService.getEngine(engineId);
            HistoryService historyService = getHistoryService(engine.getIp(), engine.getPort());
            List<WorkflowHistory> workflowHistories = historyService.getWorkflowHistories(startDate, endDate, workflowName, jobType,
                    SessionUtils.getUsername(), status, sort, dir, start, limit);
            response.getList().addAll(workflowHistories);
            response.setTotal(historyService.getTotalCountOfWorkflowHistories(startDate, endDate, workflowName, jobType, status, SessionUtils.getUsername()));
            response.setSuccess(true);
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 지정한 조건의 워크플로우 실행 이력을 조회한다.
     *
     * @param status 상태코드
     * @param sort   정렬할 컬럼명
     * @param dir    정렬 방식(ASC, DESC)
     * @param start  시작 페이지
     * @param limit  페이지당 건수
     * @return 워크플로우 실행 이력 목록
     */
    @RequestMapping(value = "actions", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getActions(@RequestParam(defaultValue = "0") long engineId,
                               @RequestParam(defaultValue = "ALL") String status,
                               @RequestParam(defaultValue = "ID") String sort,
                               @RequestParam(defaultValue = "DESC") String dir,
                               @RequestParam(defaultValue = "") String jobId,
                               @RequestParam(defaultValue = "0") int start,
                               @RequestParam(defaultValue = "16") int limit) {

        Response response = new Response();
        try {
            Engine engine = engineService.getEngine(engineId);
            HistoryService historyService = getHistoryService(engine.getIp(), engine.getPort());

            if (StringUtils.isEmpty(jobId)) {
                List<ActionHistory> actionHistories = historyService.getRunningActionHistories(SessionUtils.getUsername(), status, sort, dir);
                response.getList().addAll(actionHistories);
            } else {
                List<ActionHistory> actionHistories = historyService.getActionHistories(jobId);
                response.getList().addAll(actionHistories);
            }
            response.setSuccess(true);
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    @RequestMapping(value = "log", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getLog(@RequestParam(defaultValue = "0") long engineId, @RequestParam(defaultValue = "0") long actionId) {
        Engine engine = engineService.getEngine(engineId);
        if (engine == null) {
            ResponseEntity responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
            return responseEntity;
        } else {
            HistoryService historyService = getHistoryService(engine.getIp(), engine.getPort());
            String log = historyService.getActionLog(actionId);

            MultiValueMap headers = new HttpHeaders();
            headers.set("Content-Type", "text/plain;chartset=UTF-8");
            ResponseEntity responseEntity = new ResponseEntity(log, headers, HttpStatus.OK);
            return responseEntity;
        }
    }

    @RequestMapping(value = "script", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getScript(@RequestParam(defaultValue = "0") long engineId, @RequestParam(defaultValue = "0") long actionId) {
        Engine engine = engineService.getEngine(engineId);
        if (engine == null) {
            ResponseEntity responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
            return responseEntity;
        } else {
            HistoryService historyService = getHistoryService(engine.getIp(), engine.getPort());
            ActionHistory actionHistory = historyService.getActionHistory(actionId);
            String script = actionHistory.getScript();

            MultiValueMap headers = new HttpHeaders();
            headers.set("Content-Type", "text/plain;chartset=UTF-8");
            ResponseEntity responseEntity = new ResponseEntity(StringUtils.isEmpty(script) ? "" : script, headers, HttpStatus.OK);
            return responseEntity;
        }
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @param ip   Workflow Engine의 IP
     * @param port Workflow Engine의 Port
     * @return Remote Workflow Engine Service
     */
    private HistoryService getHistoryService(String ip, String port) {
        Engine engine = new Engine();
        engine.setIp(ip);
        engine.setPort(port);
        return getHistoryService(getHistoryServiceUrl(engine));
    }

    /**
     * Remote Workflow Engine Service를 가져온다.
     *
     * @return Remote Workflow Engine Service
     */
    private HistoryService getHistoryService(String url) {
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl(url);
        factoryBean.setServiceInterface(HistoryService.class);
        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();
        factoryBean.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        factoryBean.afterPropertiesSet();
        return (HistoryService) factoryBean.getObject();
    }

    /**
     * Remote Workflow Engine URL을 구성한다.
     *
     * @param engine Workflow Engine
     * @return Remote Workflow Engine의 URL
     */
    private String getHistoryServiceUrl(Engine engine) {
        return format("http://{}:{}/remote/history", engine.getIp(), engine.getPort()).getMessage();
    }

}
