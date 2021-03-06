/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb.regressionsuites;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;

import static junit.framework.Assert.fail;
import junit.framework.TestCase;

import org.voltdb.BackendTarget;
import org.voltdb.compiler.VoltProjectBuilder;

public class TestAdminPort extends TestCase {

    PortListener ncprocess;
    PipeToFile pf;
    int rport;

    public TestAdminPort(String name) {
        super(name);
    }

    /**
     * JUnit special method called to setup the test. This instance will start
     * the VoltDB server using the VoltServerConfig instance provided.
     */
    @Override
    public void setUp() throws Exception {
        rport = SecureRandom.getInstance("SHA1PRNG").nextInt(2000) + 22000;
        System.out.println("Random Admin port is: " + rport);
        ncprocess = new PortListener(rport);
        try {
            //Build the catalog
            VoltProjectBuilder builder = new VoltProjectBuilder();
            builder.addLiteralSchema("");
            String catalogJar = "dummy.jar";

            LocalCluster config = new LocalCluster(catalogJar, 2, 1, 0, BackendTarget.NATIVE_EE_JNI);

            config.portGenerator.enablePortProvider();
            config.portGenerator.pprovider.setAdmin(rport);
            config.setHasLocalServer(false);
            //We expect it to crash
            config.setExpectedToCrash(true);

            boolean success = config.compile(builder);
            assertTrue(success);

            config.startUp();
            pf = config.m_pipes.get(0);
            Thread.currentThread().sleep(10000);
        } catch (IOException ex) {
            fail(ex.getMessage());
        } finally {
        }
    }

    /**
     * JUnit special method called to shutdown the test. This instance will
     * stop the VoltDB server using the VoltServerConfig instance provided.
     */
    @Override
    public void tearDown() throws Exception {
        if (ncprocess != null) {
            ncprocess.close();
        }
    }

    /*
     *
     */
    public void testAdminPort() throws Exception {
        BufferedReader bi = new BufferedReader(new FileReader(new File(pf.m_filename)));
        String line;
        boolean failed = true;
        final CharSequence cs = "Client interface failed to bind to Admin port";
        while ((line = bi.readLine()) != null) {
            System.out.println(line);
            if (line.contains(cs)) {
                failed = false;
                break;
            }
        }
        assertFalse(failed);
    }
}
