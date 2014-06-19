package org.openflamingo.web.account;

import org.openflamingo.web.admin.HadoopClusterAdminService;
import org.openflamingo.web.engine.EngineService;
import org.openflamingo.web.tree.TreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private TreeService treeService;

    /**
     * Workflow Engine 정보를 얻기 위한 Engine Service.
     */
    @Autowired
    private EngineService engineService;

    /**
     * Hadoop Cluster 정보를 얻기 위한 Hadoop Cluster Admin Service.
     */
    @Autowired
    private HadoopClusterAdminService hadoopClusterAdminService;

    @Override
    public void initilizeUser(String username) {
    }

}
