/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sling.graphql.core.engine;

import org.apache.sling.graphql.api.SlingDataFetcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

/** Selects a SlingDataFetcher used to retrieve data, based
 *  on a name specified by a GraphQL schema directive.
 */
@Component(service=SlingDataFetcherSelector.class)
public class SlingDataFetcherSelector {

    /** Fetchers which have a name starting with this prefix must be
     *  under the {#link RESERVED_PACKAGE_PREFIX} package.
     */
    public static final String RESERVED_NAME_PREFIX = "sling/";

    /** Package name prefix for fetchers which have names starting
     *  with the {#link RESERVED_NAME_PREFIX}.
     */
    public static final String RESERVED_PACKAGE_PREFIX = "org.apache.sling.";

    private BundleContext bundleContext;

    @Reference
    private ScriptedDataFetcherProvider scriptedDataFetcherProvider;

    @Activate
    public void activate(BundleContext ctx) {
        bundleContext = ctx;
    }

    /** Return a SlingFetcher from the available OSGi services, if there's one
     *  registered with the supplied name.
      */
      @SuppressWarnings("unchecked")
      private SlingDataFetcher<Object> getOsgiServiceFetcher(@NotNull String name) throws IOException {
        SlingDataFetcher<Object> result = null;
        final String filter = String.format("(%s=%s)", SlingDataFetcher.NAME_SERVICE_PROPERTY, name);
        ServiceReference<?>[] refs= null;
        try {
            refs = bundleContext.getServiceReferences(SlingDataFetcher.class.getName(), filter);
        } catch(InvalidSyntaxException ise) {
            throw new IOException("Invalid OSGi filter syntax", ise);
        }
        if(refs != null) {
            // SlingFetcher services must have a unique name
            if(refs.length > 1) {
                throw new IOException(String.format("Got %d services for %s, expected just one", refs.length, filter));
            }
            result = (SlingDataFetcher<Object>)bundleContext.getService(refs[0]);
            validateResult(name, result);
        }
        return result;
    }

    private void validateResult(String name, SlingDataFetcher<?> fetcher) throws IOException {
        if(name.startsWith(RESERVED_NAME_PREFIX)) {
            final String className = fetcher.getClass().getName();
            if(!fetcher.getClass().getName().startsWith(RESERVED_PACKAGE_PREFIX)) {
                throw new IOException(
                    String.format(
                        "Invalid SlingDataFetcher %s:"
                        + " fetcher names starting with '%s' are reserved for Apache Sling Java packages", 
                        className, RESERVED_NAME_PREFIX));
            }
        }
    }

    /** @return a SlingDataFetcher, or null if none available. First tries to get an
     *  OSGi SlingDataFetcher service, and if not found tries to find a scripted SlingDataFetcher.
     */
    @Nullable
    public SlingDataFetcher<Object> getSlingFetcher(@NotNull String name) throws IOException {
        SlingDataFetcher<Object> result = getOsgiServiceFetcher(name);
        if(result == null) {
            result = scriptedDataFetcherProvider.getDataFetcher(name);
        }
        return result;
    }
}
