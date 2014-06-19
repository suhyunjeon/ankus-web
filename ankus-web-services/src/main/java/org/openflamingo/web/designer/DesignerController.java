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
package org.openflamingo.web.designer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.openflamingo.model.rest.*;
import org.openflamingo.model.workflow.PreviewFile;
import org.openflamingo.util.ExceptionUtils;
import org.openflamingo.web.admin.HadoopClusterAdminService;
import org.openflamingo.web.configuration.ConfigurationManager;
import org.openflamingo.web.core.LocaleSupport;
import org.openflamingo.web.engine.EngineService;
import org.openflamingo.web.security.SessionUtils;
import org.openflamingo.web.tree.TreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Workflow Designer Controller.
 *
 * @author Edward KIM
 * @version ankus 0.2.1
 * @modify Suhyun Jeon
 */
@Controller
@RequestMapping("/designer")
public class DesignerController extends LocaleSupport {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(DesignerController.class);

    /**
     * ROOT 노드의 ID
     */
    private final static String ROOT = "/";

    /**
     * Workflow Tree Service
     */
    @Autowired
    private TreeService treeService;

    /**
     * Designer Service
     */
    @Autowired
    private DesignerService designerService;

    @Autowired
    private EngineService engineService;

    @Autowired
    private HadoopClusterAdminService hadoopClusterAdminService;


    /**
     * 워크플로우를 등록한다.
     *
     * @param workflowId   워크플로우 식별자 ID
     * @param treeId       트리 노드의 ID
     * @param parentTreeId 부모 트리 노드의 ID
     * @param clusterId    Hadoop Cluster의 ID
     * @param xml          OpenGraph XML
     * @return HTTP Response
     */
    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save(@RequestParam(defaultValue = "-1") Long id,
                         @RequestParam(defaultValue = "") String workflowId,
                         @RequestParam(defaultValue = "-1") Long treeId,
                         @RequestParam(defaultValue = "") String parentTreeId,
                         @RequestParam(defaultValue = "0") Long clusterId,
                         @RequestBody String xml) {

        if (logger.isDebugEnabled()) {
            logger.debug("Request OpenGraph XML is \n{}", xml);
        }

        Response response = new Response();
        try {
            Workflow workflow = null;
            if (id > -1) {
                workflow = designerService.update(treeId, id, xml, SessionUtils.getUsername());
            } else {
                workflow = designerService.regist(parentTreeId, xml, SessionUtils.getUsername());
            }

            response.getMap().put("instance_id", workflow.getWorkflowId());
            response.getMap().put("cluster", clusterId); // FIXME
            response.getMap().put("id", String.valueOf(workflow.getId()));
            response.getMap().put("tree_id", String.valueOf(workflow.getWorkflowTreeId()));
            response.getMap().put("name", String.valueOf(workflow.getWorkflowName()));
            response.getMap().put("desc", String.valueOf(workflow.getDescription()));
            response.setSuccess(true);
        } catch (Exception ex) {
            String message = message("S_DESIGNER", "CANNOT_REGIST_WORKFLOW", workflowId, ex.getMessage());
            logger.warn("{}", message, ex);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            response.setSuccess(false);
        }
        return response;
    }

    /**
     * 워크플로우를 로딩한다. 만약 OpenGraph 기반 워크플로우가 존재하지 않는다면 CLI를 통해서 등록했다는 가정을 할 수 있으므로
     * 클라이언트에게 에러 코드를 전달한다.
     *
     * @return Response REST JAXB Object
     */
    @RequestMapping(value = "load", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> load(@RequestParam Long id) {
        MultiValueMap headers = new HttpHeaders();
        try {
            String designerXml = designerService.load(id);
            headers.set("Content-Type", "text/plain; charset=UTF-8");
            return new ResponseEntity<String>(designerXml, headers, HttpStatus.OK);
        } catch (Exception ex) {
            String message = message("S_DESIGNER", "CANNOT_LOAD_WORKFLOW", id.toString(), ex.getMessage());
            logger.warn("{}", message, ex);
            headers.set("Content-Type", "text/plain; charset=UTF-8");
            return new ResponseEntity<String>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 워크플로우를 로딩한다. 만약 OpenGraph 기반 워크플로우가 존재하지 않는다면 CLI를 통해서 등록했다는 가정을 할 수 있으므로
     * 클라이언트에게 에러 코드를 전달한다.
     *
     * @return Response REST JAXB Object
     */
    @RequestMapping(value = "previewHDFSFile", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response previewHDFSFile(@RequestParam String inputPath, String delimiter, long engineId) {
        Response response = new Response();

        try {
            Configuration configuration = new Configuration();

            String hadoopPath = ConfigurationManager.getConfigurationManager().get("hadoop.home");
            // Set HADOOP_HOME in local system path
            configuration.addResource(new Path(hadoopPath + "/conf/core-site.xml"));
            configuration.addResource(new Path(hadoopPath + "/conf/hdfs-site.xml"));

            // Get hadoop cluster
            Engine engine = engineService.getEngine(engineId);
            if (engine == null) {
                throw new IllegalArgumentException(message("S_DESIGNER", "NOT_VALID_WORKFLOW_ENG"));
            }
            HadoopCluster hadoopCluster = hadoopClusterAdminService.getHadoopCluster(engine.getHadoopClusterId());
            String hdfsUrl = hadoopCluster.getHdfsUrl();

            FileSystem fileSystem = FileSystem.get(configuration);
            Path path = new Path(hdfsUrl + inputPath);

            if (!fileSystem.isFile(path)) {
                logger.error("{}", "Input should be a file.");
            }

            List<PreviewFile> list = new ArrayList<PreviewFile>();
            PreviewFile previewFile = new PreviewFile();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileSystem.open(path)));

            String lines;
            int count = 0;
            int keyNumber = -1;

            List<Integer> columnIndexList = new ArrayList<Integer>();
            ArrayList<String> rowDataList = new ArrayList<String>();
            Map map = new HashMap();
            String[] splits = {};

            while ((lines = bufferedReader.readLine()) != null) {
                count++;
                if (count < 6) {
                    keyNumber++;
                    splits = lines.split(delimiter);
                    map.put(keyNumber, lines);
                }
            }

            int columnLength = splits.length;
            StringBuffer stringBuffer = new StringBuffer();

            for (int i = 0; i <= columnLength - 1; i++) {
                columnIndexList.add(i);
                for (Object line : map.values()) {
                    stringBuffer.append(splits[i]).append(",");
                }
                stringBuffer.append("::");
            }

            for (String row : stringBuffer.toString().split(",::")) {
                rowDataList.add(row + "...");
            }

            //Set field number
            previewFile.setColumnIndex(columnIndexList);
            //Set field data
            previewFile.setRowData(rowDataList);
            list.add(previewFile);

            response.getList().addAll(list);
            response.setObject(delimiter);
            response.setTotal(columnLength);
            response.setSuccess(true);

            //Closed bufferedReader
            bufferedReader.close();

            return response;

        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage(), ex);
            response.getError().setMessage(ex.getMessage());
            response.setSuccess(false);
            return response;
        }
    }


    /**
     * 워크플로우를 로딩한다.
     *
     * @return Response REST JAXB Object
     */
    @RequestMapping(value = "get", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response get(@RequestParam Long id) {
        Response response = new Response();
        try {
            Workflow workflow = designerService.getWorkflow(id);
            response.setSuccess(true);
            response.setObject(workflow);
        } catch (Exception ex) {
            String message = message("S_DESIGNER", "CANNOT_LOAD_WORKFLOW", id.toString(), ex.getMessage());
            logger.warn("{}", message, ex);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            response.setSuccess(false);
        }
        return response;
    }

    @RequestMapping(value = "run", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response run(@RequestBody Map<String, String> params) {
        long id = Long.parseLong(params.get("id"));
        Response response = new Response();
        try {
            designerService.run(id, Long.parseLong(params.get("engineId")));

            Workflow workflow = designerService.getWorkflow(id);
            response.getMap().put("instance_id", workflow.getWorkflowId());
            response.getMap().put("id", String.valueOf(workflow.getId()));
            response.getMap().put("tree_id", String.valueOf(workflow.getWorkflowTreeId()));
            response.getMap().put("name", String.valueOf(workflow.getWorkflowName()));
            response.getMap().put("desc", String.valueOf(workflow.getDescription()));
            response.setSuccess(true);
        } catch (Exception ex) {
            String message = message("S_DESIGNER", "CANNOT_RUN_WORKFLOW", Long.toString(id), ex.getMessage());
            logger.warn("{}", message, ex);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
            response.setSuccess(false);
        }
        return response;
    }

    /**
     * 워크플로우 XML을 HTML로 변환한다.
     *
     * @param id 워크플로우의 식별자
     * @return 워크플로우 XML
     */
    @RequestMapping(value = "xml", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getWorkflowXml(@RequestParam(defaultValue = "-1") long id) {
        Response response = new Response();
        try {
            Workflow workflow = designerService.getWorkflow(id);
            String xml = workflow.getWorkflowXml();
            response.setSuccess(true);
            response.getMap().put("xml", OpenGraphMarshaller.escape(xml));
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 워크플로우 목록을 확인한다.
     *
     * @param type 트리 노드의 유형
     * @param node 워크플로우 노드
     * @return HTTP REST Response JAXB Object
     */
    @RequestMapping(value = "tree/get", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response get(@RequestParam String type, @RequestParam String node) {
        logger.debug("[WF][TREE][GET] Type '{}' Path '{}'", type, node);

        Response response = new Response();

        Tree treeNode = null;
        if (ROOT.equals(node)) {
            // if root, return root node
            treeNode = treeService.getRoot(TreeType.valueOf(type.trim()), SessionUtils.getUsername());

            // if root does not exist, create root.
            if (treeNode == null) {
                treeNode = treeService.createRoot(TreeType.valueOf(type.trim()), SessionUtils.getUsername());
            }
        } else {
            // ROOT 노드가 아니라면 PK인 Tree Id를 부모 노드로 설정한다.
            treeNode = treeService.get(Long.parseLong(node));
        }

        // Get childs from parent.
        List<Tree> childs = treeService.getWorkflowChilds(treeNode.getId());
        for (Tree tree : childs) {
            Map map = new HashMap();
            map.put("id", tree.getId());
            if (NodeType.FOLDER.equals(tree.getNodeType())) {
                map.put("cls", "folder");
            } else {
                setStatus(tree.getId(), map);
            }
            map.put("text", tree.getName());
            map.put("workflowId", tree.getReferenceId());
            map.put("leaf", NodeType.FOLDER.equals(tree.getNodeType()) ? false : true);
            response.getList().add(map);
        }
        response.setSuccess(true);
        return response;
    }

    /**
     * 워크플로우의 상태코드를 확인한다.
     *
     * @param workflowId 워크플로우의 식별자
     * @return 워크플로우의 상태코드
     */
    @RequestMapping(value = "status", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getWorkflowStatus(@RequestParam(defaultValue = "-1") long workflowId) {
        Response response = new Response();
        response.setSuccess(true);
        if (workflowId < 0) {
            response.getMap().put("count", "0");
            return response;
        }
        try {
            Workflow workflow = designerService.getWorkflow(workflowId);
            response.getMap().put("status", workflow.getStatus());
            response.getMap().put("id", workflow.getId());
            response.getMap().put("instance_id", workflow.getWorkflowId());
            response.getMap().put("tree_id", workflow.getWorkflowTreeId());
            response.getMap().put("name", workflow.getWorkflowName());
            // response.getMap().put("count", jobService.getScheduledCountByWorkflowId(workflow.getId()));
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 지정한 워크플로우를 삭제한다. 삭제는 워크플로우의 식별자를 기준으로 하지 않고
     * TREE의 식별자를 기준으로 한다.
     *
     * @return Response REST JAXB Object
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response deleteWorkflow(@RequestBody Map<String, String> params) {
        Response response = new Response();
        try {
            String nodeType = params.get("nodeType");
            if ("folder".equals(nodeType)) {
                treeService.delete(Long.parseLong(params.get("id")));
            } else {
                designerService.delete(Long.parseLong(params.get("id")), Long.parseLong(params.get("workflowId")));
            }
            response.setSuccess(true);
        } catch (Exception ex) {
            String message = message("S_DESIGNER", "CANNOT_DELETE_SELECTION", params.get("text"), ex.getMessage());
            logger.warn("{}", message, ex);

            response.setSuccess(false);
            response.getError().setMessage(ex.getMessage());
            if (ex.getCause() != null) response.getError().setCause(ex.getCause().getMessage());
            response.getError().setException(ExceptionUtils.getFullStackTrace(ex));
        }
        return response;
    }

    /**
     * 지정한 워크플로우의 상태 코드의 CSS와 해당 워크플로우의 배치 개수를 설정한다.
     *
     * @param treeId 워크플로우의 상태 코드를 확인할 Tree ID
     */
    private void setStatus(long treeId, Map map) {
/*
        Workflow workflow = jobService.getWorkflowByTeeId(treeId);
        Long count = jobService.getCountByWorkflowId(workflow.getId());
        map.put("job", count);
        if (StringUtils.isEmpty(workflow.getDesignerXml())) {
            map.put("iconCls", "designer_not_load");
            map.put("qtip", "CLI를 통해 등록한 워크플로우 (" + workflow.getWorkflowId() + ")");
            return;
        }
        if (count > 0) {
            map.put("iconCls", "designer_not_remove");
            map.put("qtip", "등록되어 있는 배치 작업의 개수 :: " + count + "개 (" + workflow.getWorkflowId() + ")");
            return;
        }
        map.put("iconCls", "designer_load");
        map.put("qtip", workflow.getWorkflowId());
*/

        map.put("iconCls", "status-blue");
    }
}
