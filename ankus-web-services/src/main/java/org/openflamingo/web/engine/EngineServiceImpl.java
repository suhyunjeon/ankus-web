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

import org.openflamingo.core.exception.SystemException;
import org.openflamingo.model.rest.Engine;
import org.openflamingo.web.core.LocaleSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineServiceImpl extends LocaleSupport implements EngineService {

    @Autowired
    private EngineRepository engineRepository;

    @Override
    public boolean removeEngine(Long id) {
        boolean deleted = engineRepository.delete(id) > 0;
        if (!deleted) {
            throw new SystemException(message("S_ENGINE", "CANNOT_REMOVE", null));
        }
        return deleted;
    }

    @Override
    public List<Engine> getEngines() {
        return engineRepository.selectAll();
    }

    @Override
    public boolean addEngine(Engine engine) {
        boolean inserted = engineRepository.insert(engine) > 0;
        if (!inserted) {
            throw new SystemException(message("S_ENGINE", "CANNOT_INSERT", null));
        }
        return inserted;
    }

    @Override
    public Engine getEngine(Long serverId) {
        Engine selected = engineRepository.select(serverId);
        if (selected == null) {
            throw new SystemException(message("S_ENGINE", "NOT_FOUND_ENGINE", null));
        }
        return selected;
    }
}
