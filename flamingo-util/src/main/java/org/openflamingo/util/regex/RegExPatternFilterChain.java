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
package org.openflamingo.util.regex;

import java.util.List;

/**
 * Regular Expression Pattern 기반 문자열 패턴 필터를 하나 이상 적용할 수 있는 필터 체인.
 *
 * @author Edward KIM
 * @version 0.3
 */
public interface RegExPatternFilterChain extends RegExPatternFilter {

    /**
     * 주어진 문자열을 Regular Expression을 기반으로 필터링한다.
     *
     * @param message Regular Expression으로 필터링할 문자열
     * @return 필터링한 문자열
     */
    List<String> filter(String message);

}
