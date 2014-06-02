/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openflamingo.mapreduce.aggregator;


import org.apache.hadoop.io.FloatWritable;

/**
 * Aggregator for summing up float values.
 */
public class FloatSumAggregator implements Aggregator<FloatWritable> {

    /**
     * Aggregated sum
     */
    private float sum = 0;

    /**
     * Aggregate a primitive float.
     *
     * @param value Float value to aggregate.
     */
    public void aggregate(float value) {
        sum += value;
    }

    @Override
    public void aggregate(FloatWritable value) {
        sum += value.get();
    }

    /**
     * Set aggregated value using a primitive float.
     *
     * @param value Float value to set.
     */
    public void setAggregatedValue(float value) {
        sum = value;
    }

    @Override
    public void setAggregatedValue(FloatWritable value) {
        sum = value.get();
    }

    @Override
    public FloatWritable getAggregatedValue() {
        return new FloatWritable(sum);
    }

    @Override
    public FloatWritable createAggregatedValue() {
        return new FloatWritable();
    }

}
