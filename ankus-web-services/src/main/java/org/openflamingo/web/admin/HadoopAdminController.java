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
package org.openflamingo.web.admin;

import org.openflamingo.model.rest.HadoopCluster;
import org.openflamingo.model.rest.Response;
import org.openflamingo.web.core.LocaleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/hadoop")
public class HadoopAdminController extends LocaleSupport {

    private Logger logger = LoggerFactory.getLogger(HadoopAdminController.class);

    @Autowired
    private HadoopClusterAdminService adminService;

    @RequestMapping(value = "clusters", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response getHadoopClusters() {
        Response response = new Response();

        try {
            response.setSuccess(true);
            List<HadoopCluster> servers = adminService.getHadoopClusters();
            if (servers != null) {
                response.getList().addAll(servers);
                response.setTotal(response.getList().size());
            } else {
                response.setTotal(0);
            }
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(message("S_ADMIN", "CANNOT_CHECK_HCLUSTER_INFO"));
            response.getError().setCause(ex.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response add(@RequestBody HadoopCluster hadoopCluster) {
        Response response = new Response();
        if (StringUtils.isEmpty(hadoopCluster.getName().trim())) {
            response.setSuccess(false);
            return response;
        }

        try {
            response.setSuccess(adminService.addHadoopCluster(hadoopCluster));
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(message("S_ADMIN", "CANNOT_ADD_HCLUSTER"));
            response.getError().setCause(ex.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "exist", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response exist(@RequestBody HadoopCluster hadoopCluster) {
        Response response = new Response();
        if (StringUtils.isEmpty(hadoopCluster.getName())) {
            response.setSuccess(false);
            return response;
        }

        try {
            response.setSuccess(adminService.exist(hadoopCluster));
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(message("S_ADMIN", "CANNOT_CHECK_HCLUSTER"));
            response.getError().setCause(ex.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response delete(@RequestBody HadoopCluster hadoopCluster) {
        Response response = new Response();
        try {
            response.setSuccess(adminService.deleteHadoopCluster(hadoopCluster.getId()));
            response.setSuccess(true);
        } catch (Exception ex) {
            response.setSuccess(false);
            response.getError().setMessage(message("S_ADMIN", "CANNOT_DELETE_HCLUSTER"));
            response.getError().setCause(ex.getMessage());
        }
        return response;
    }

}