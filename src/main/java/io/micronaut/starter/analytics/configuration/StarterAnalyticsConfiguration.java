/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.analytics.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Allows configuration of starter analytics.
 */
@ConfigurationProperties(StarterAnalyticsConfiguration.PREFIX)
public class StarterAnalyticsConfiguration {
    public static final String PREFIX = "micronaut.starter.analytics";

    private int pageSize = 10000;

    private int days = 30;

    private int maxDays = 365;

    /**
     * @return The page size to fetch applications from database
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size to fetch applications from database
     * @param pageSize The page size
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(int maxDays) {
        this.maxDays = maxDays;
    }
}
