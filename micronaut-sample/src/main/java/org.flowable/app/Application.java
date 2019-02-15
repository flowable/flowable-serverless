/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.app;

import io.micronaut.runtime.Micronaut;

/**
 * Try with
 *
 * curl -X POST -H "Content-Type: text/plain" --data 1 localhost:8083/process
 * curl -X POST -H "Content-Type: text/plain" --data 2 localhost:8083/process
 *
 * To build graalvm image, run ./build-native-image.sh
 */
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}