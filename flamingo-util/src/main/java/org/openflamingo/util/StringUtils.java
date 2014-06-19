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

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * String Utility.
 *
 * @author Edward KIM
 * @since 0.1
 */
public class StringUtils {

    /**
     * 문자열 리스트를 문자열 배열로 변환한다.
     *
     * @param values 문자열 리스트
     * @return 문자열 배열
     */
    public static String[] collectionToStringArray(List<String> values) {
        String[] array = new String[values.size()];
        int index = 0;
        for (String value : values) {
            array[index] = value;
            index++;
        }
        return array;
    }

    /**
     * 지정한 문자열이 비어있는지 확인한다.
     *
     * @param str 테스트할 문자열
     * @return 비어있다면 <tt>true</tt>
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() < 1);
    }

    /**
     * 콤마를 구분자로 리스트를 하나의 문자열을 구성한다.
     *
     * @param values 리스트
     * @return 콤마 구분자를 포함한 문자열
     */
    public static String collectionToCommaDelimitedString(List<String> values) {
        return collectionToDelimitedString(values, ",");
    }

    /**
     * 지정한 구분자로 리스트를 하나의 문자열로 구성한다.
     *
     * @param values    리스트
     * @param delimiter 구분자
     * @return 구분자를 포함한 문자열
     */
    public static String collectionToDelimitedString(List<String> values, String delimiter) {
        return Joiner.on(delimiter).join(values);
    }

    /**
     * 문자열 배열을 리스트로 변환한다.
     *
     * @param values 문자열 배열열
     * @return 문자열 리스트
     */
    public static List<String> arrayToCollection(String[] values) {
        List<String> list = new ArrayList<String>(values.length);
        Collections.addAll(list, values);
        return list;
    }

    /**
     * 구분자로 분리 가능한 문자열을 리스트로 변환한다.
     *
     * @param value     구분자를 포함한 리스트
     * @param separator 구분자
     * @return 문자열 리스트
     */
    public static List<String> stringToCollection(String value, String separator) {
        String[] strings = org.apache.commons.lang.StringUtils.splitPreserveAllTokens(value, separator);
        return arrayToCollection(strings);
    }

    /**
     * 전체 문자열에서 패턴을 검색하여 새로운 패턴을 적용한다.
     * <p>
     * <pre>
     * String result = StringUtils.replace("Hello World", "Hello", "World"); // "World World"
     * </pre>
     * </p>
     *
     * @param inString   전체 문자열
     * @param oldPattern 검색할 문자열 패턴
     * @param newPattern 새로 적용할 문자열 패턴
     * @return 교체된 문자열
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        return org.springframework.util.StringUtils.replace(inString, oldPattern, newPattern);
    }
}
