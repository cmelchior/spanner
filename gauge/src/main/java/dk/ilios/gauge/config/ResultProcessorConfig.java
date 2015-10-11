/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original author: gak@google.com (Gregory Kick)
 */

package dk.ilios.gauge.config;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * This is the configuration passed to the {@link dk.ilios.gauge.http.ResultProcessor} by the
 * user.
 *
 * @author
 */
public class ResultProcessorConfig {
    private final String className;
    private final ImmutableMap<String, String> options;

    private ResultProcessorConfig(Builder builder) {
        this.className = builder.className;
        this.options = builder.optionsBuilder.build();
    }

    public String className() {
        return className;
    }

    public ImmutableMap<String, String> options() {
        return options;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ResultProcessorConfig) {
            ResultProcessorConfig that = (ResultProcessorConfig) obj;
            return this.className.equals(that.className)
                    && this.options.equals(that.options);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(className, options);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("className", className)
                .add("options", options)
                .toString();
    }

    public static final class Builder {
        private String className;
        private ImmutableMap.Builder<String, String> optionsBuilder = ImmutableMap.builder();

        public Builder className(String className) {
            this.className = checkNotNull(className);
            return this;
        }

        public Builder addOption(String option, String value) {
            this.optionsBuilder.put(option, value);
            return this;
        }

        public Builder addAllOptions(Map<String, String> options) {
            this.optionsBuilder.putAll(options);
            return this;
        }

        public ResultProcessorConfig build() {
            return new ResultProcessorConfig(this);
        }
    }
}