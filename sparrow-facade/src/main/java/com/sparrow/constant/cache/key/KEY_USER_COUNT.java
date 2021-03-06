/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.constant.cache.key;

import com.sparrow.constant.SPARROW_MODULE;
import com.sparrow.constant.cache.KEY;

public class KEY_USER_COUNT {

    /**
     * 用户count
     */
    public static final KEY.Business COUNT = new KEY.Business(SPARROW_MODULE.COUNT, "USER");
    public static final KEY.Business PUBLISHED = new KEY.Business(SPARROW_MODULE.COUNT, "PUBLISHED");
    public static final KEY.Business ATTENTION = new KEY.Business(SPARROW_MODULE.COUNT, "ATTENTION");
    public static final KEY.Business FANS = new KEY.Business(SPARROW_MODULE.COUNT, "FANS");
}