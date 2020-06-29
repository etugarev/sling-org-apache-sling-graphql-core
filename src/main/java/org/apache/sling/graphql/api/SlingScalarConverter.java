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

package org.apache.sling.graphql.api;

import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * A service that parses and serializes a custom GraphQL Scalar by 
 * converting between an eXternal type X an an inTernal one T.
 * 
 * Instances of this service must have a {@link SlingScalarConverter.NAME}
 * service property which is the name of the scalar type.
 */
@ConsumerType
public interface SlingScalarConverter<T, X> {
    
    String NAME_SERVICE_PROPERTY = "name";

    /** Parse an external value (a query argument for example) into its internal representation */
    @Nullable 
    T parseValue(@Nullable X input) throws ScalarConversionException;

    /** Serialize an internal value (provided by a {@link SlingDataFetcher} into its
     *  external representation.
     */
    @Nullable
    X serialize(@Nullable T value) throws ScalarConversionException;

    /**
     * Return converter name
     */
    String getName();

    /**
     * Return converter description
     */
    String getDescription();
}
