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


import org.apache.hadoop.io.IntWritable;

/**
 * Aggregator for getting max integer value.
 */
public class IntMaxAggregator implements Aggregator<IntWritable> {

    /**
     * Saved maximum value
     */
    private int max = Integer.MIN_VALUE;

    /**
     * Aggregate with a primitive integer.
     *
     * @param value Integer value to aggregate.
     */
    public void aggregate(int value) {
        int val = value;
        if (val > max) {
            max = val;
        }
    }

    @Override
    public void aggregate(IntWritable value) {
        int val = value.get();
        if (val > max) {
            max = val;
        }
    }

    /**
     * Set aggregated value using a primitive integer.
     *
     * @param value Integer value to set.
     */
    public void setAggregatedValue(int value) {
        max = value;
    }

    @Override
    public void setAggregatedValue(IntWritable value) {
        max = value.get();
    }

    @Override
    public IntWritable getAggregatedValue() {
        return new IntWritable(max);
    }

    @Override
    public IntWritable createAggregatedValue() {
        return new IntWritable();
    }
}
