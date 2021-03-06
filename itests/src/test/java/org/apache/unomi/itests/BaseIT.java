/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.apache.unomi.itests;

import org.junit.Assert;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

/**
 * Base class for integration tests.
 * 
 * @author kevan
 */
public abstract class BaseIT {
    
    protected static final String HTTP_PORT = "8181";
    protected static final String URL = "http://localhost:" + HTTP_PORT;
    protected static final String KARAF_DIR = "target/exam";
    protected static final String UNOMI_KEY = "670c26d1cc413346c3b2fd9ce65dab41";

    @Configuration
    public Option[] config() throws InterruptedException {
        MavenArtifactUrlReference karafUrl = maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .version("4.1.5")
                .type("tar.gz");

        MavenUrlReference karafStandardRepo = maven()
                .groupId("org.apache.karaf.features")
                .artifactId("standard")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        MavenUrlReference karafCellarRepo = maven()
                .groupId("org.apache.karaf.cellar")
                .artifactId("apache-karaf-cellar")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        MavenUrlReference karafPaxWebRepo = maven()
                .groupId("org.ops4j.pax.web")
                .artifactId("pax-web-features")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        MavenUrlReference karafCxfRepo = maven()
                .groupId("org.apache.cxf.karaf")
                .artifactId("apache-cxf")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        MavenUrlReference contextServerRepo = maven()
                .groupId("org.apache.unomi")
                .artifactId("unomi-kar")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        MavenUrlReference routerRepo = maven()
                .groupId("org.apache.unomi")
                .artifactId("unomi-router-karaf-feature")
                .classifier("features")
                .type("xml")
                .versionAsInProject();

        return new Option[]{
                debugConfiguration("5006", false),
                karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(new File(KARAF_DIR))
                        .useDeployFolder(true),
                replaceConfigurationFile("etc/org.apache.unomi.router.cfg", new File(
                        "src/test/resources/org.apache.unomi.router.cfg")),
                replaceConfigurationFile("data/tmp/1-basic-test.csv", new File(
                        "src/test/resources/1-basic-test.csv")),
                replaceConfigurationFile("data/tmp/recurrent_import/2-surfers-test.csv", new File(
                        "src/test/resources/2-surfers-test.csv")),
                replaceConfigurationFile("data/tmp/recurrent_import/3-surfers-overwrite-test.csv", new File(
                        "src/test/resources/3-surfers-overwrite-test.csv")),
                replaceConfigurationFile("data/tmp/recurrent_import/4-surfers-delete-test.csv", new File(
                        "src/test/resources/4-surfers-delete-test.csv")),
                replaceConfigurationFile("data/tmp/recurrent_import/5-ranking-test.csv", new File(
                        "src/test/resources/5-ranking-test.csv")),
                replaceConfigurationFile("data/tmp/recurrent_import/6-actors-test.csv", new File(
                        "src/test/resources/6-actors-test.csv")),
                replaceConfigurationFile("data/tmp/testLogin.json", new File(
                        "src/test/resources/testLogin.json")),
                replaceConfigurationFile("data/tmp/testLoginEventCondition.json", new File(
                        "src/test/resources/testLoginEventCondition.json")),
                keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevel.INFO),
                editConfigurationFilePut("etc/org.ops4j.pax.logging.cfg", "log4j2.rootLogger.level", "INFO"),
                editConfigurationFilePut("etc/org.apache.karaf.features.cfg", "serviceRequirements", "disable"),
//                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", HTTP_PORT),
//                systemProperty("org.osgi.service.http.port").value(HTTP_PORT),
                systemProperty("org.ops4j.pax.exam.rbc.rmi.port").value("1199"),
                systemProperty("org.apache.unomi.itests.elasticsearch.transport.port").value("9500"),
                systemProperty("org.apache.unomi.itests.elasticsearch.cluster.name").value("contextElasticSearchITests"),
                systemProperty("org.apache.unomi.itests.elasticsearch.http.port").value("9400"),
                systemProperty("org.apache.unomi.itests.elasticsearch.bootstrap.seccomp").value("false"),
                systemProperty("org.apache.unomi.hazelcast.group.name").value("cellar"),
                systemProperty("org.apache.unomi.hazelcast.group.password").value("pass"),
                systemProperty("org.apache.unomi.hazelcast.network.port").value("5701"),
                systemProperty("org.apache.unomi.hazelcast.tcp-ip.members").value("127.0.0.1"),
                systemProperty("org.apache.unomi.hazelcast.tcp-ip.interface").value("127.0.0.1"),
                systemProperty("unomi.autoStart").value("true"),
                features(karafCxfRepo, "cxf"),
                features(karafCellarRepo, "cellar"),
                features(contextServerRepo, "unomi-kar"),
                features(routerRepo, "unomi-router-karaf-feature"),
                CoreOptions.bundleStartLevel(100),
                CoreOptions.frameworkStartLevel(100)
        };
    }

    protected <T> T keepTrying(Supplier<T> call, Predicate<T> predicate, int timeout, int retries) throws InterruptedException {
        int count = 0;
        T value = null;
        while (value == null || !predicate.test(value)) {
            if (count++ > retries) {
                Assert.fail();
            }
            Thread.sleep(timeout);
            value = call.get();
        }
        return value;
    }
}
