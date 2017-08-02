// Copyright (c) 2012 Virtual Instruments Corporation
// Copyright (c) 2017 Luca Filipozzi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Authored by: Mihailo Despotovic
// Modified by: Luca Filipozzi

package net.emyr.aws.logging.log4jappenders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Configuration variables and static utility methods.
 */
public class Config {

    // these variables can be be supplied by the environment
    protected static final String ENV_LOG_STREAM_NAME = System.getProperty("LOG_STREAM_NAME");

    // internal hard-coded defaults
    protected static final int AWS_DRAIN_LIMIT = 256; // 1MB of 4K messages -- estimate
    protected static final int AWS_LOG_STREAM_MAX_QUEUE_DEPTH = 10000;
    protected static final int AWS_CONNECTION_TIMEOUT_MILLIS = 5000;
    protected static final int SHUTDOWN_TIMEOUT_MILLIS = 10000;

    // defaults that can be overridden by the log4j configuration
    protected static final String DEFAULT_LOG_APPENDER_NAME = "CloudWatchAppender";
    protected static final String DEFAULT_AWS_LOG_GROUP_NAME = "unknown";
    protected static final int AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS = 5;

    // AWS metadata service URL
    protected static final String AWS_INSTANCE_METADATA_SERVICE_URL = "http://169.254.169.254/latest/meta-data/instance-id";

    /**
     * Retrieve AWS instance id you are running on.
     *
     * @return instanceId the instance id
     */
    public static String retrieveInstanceId() {
        String hostId;
        try {
            hostId = InetAddress.getLocalHost().getHostName(); // in some unlikely case the AWS metadata service is down
        } catch (UnknownHostException ux) {
            hostId = "unknown";
        }
        String inputLine;
        try {
            final URL EC2MetaData = new URL(AWS_INSTANCE_METADATA_SERVICE_URL);
            final URLConnection EC2MD = EC2MetaData.openConnection();
            EC2MD.setConnectTimeout(AWS_CONNECTION_TIMEOUT_MILLIS);
            final BufferedReader in = new BufferedReader(new InputStreamReader(EC2MD.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                hostId = inputLine;
            }
            in.close();
        } catch (IOException iox) {
            System.out.println(new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date()) +
                    " CloudWatchAppender: Could not connect to AWS instance metadata service, using " + hostId + " for the hostId.");
        }
        return hostId;
    }
}
