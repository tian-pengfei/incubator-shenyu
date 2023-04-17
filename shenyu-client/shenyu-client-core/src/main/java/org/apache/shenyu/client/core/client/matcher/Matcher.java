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

package org.apache.shenyu.client.core.client.matcher;

import java.util.Objects;

public interface Matcher<T> {

    boolean match(T element);

    default Matcher<T> and(Matcher<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> match(t) && other.match(t);
    }

    default Matcher<T> or(Matcher<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> match(t) || other.match(t);
    }

    default Matcher<T> negate() {
        return (t) -> !match(t);
    }

    static <T> Matcher<T> not(Matcher<? super T> target) {
        Objects.requireNonNull(target);
        return (Matcher<T>) target.negate();
    }
}
