/**
 * Copyright (c) 2015, piotrkot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.piokot.ftp;

import com.piokot.ftp.api.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FTP} class.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Slf4j
public final class FTPTest {
    /**
     * FTP name name.
     */
    private static final String NAME = "name";
    /**
     * FTP name password.
     */
    private static final String PASS = "pass";
    /**
     * FTP server port.
     * @checkstyle MagicNumber (2 lines)
     */
    private static final int PORT = 2222;
    /**
     * FTP server.
     */
    private transient FtpServer server;
    /**
     * Sets up Apache FTP server.
     *
     * @throws FtpException When it fails.
     */
    @Before
    public void setUp() throws FtpException {
        final UserManager manager = new PropertiesUserManagerFactory()
            .createUserManager();
        final BaseUser user = new BaseUser();
        final ListenerFactory listener = new ListenerFactory();
        final FtpServerFactory factory = new FtpServerFactory();
        user.setName(NAME);
        user.setPassword(PASS);
        user.setHomeDirectory("/tmp");
        manager.save(user);
        listener.setPort(PORT);
        factory.setUserManager(manager);
        factory.addListener("default", listener.createListener());
        this.server = factory.createServer();
        this.server.start();
    }

    /**
     * Stops FTP server.
     */
    @After
    public void cleanUp() {
        this.server.stop();
    }

    /**
     * Can list FTP files.
     *
     * @throws Exception If it fails.
     */
    @Test
    public void listFTPFiles() throws Exception {
        new FTP("localhost", this.PORT, this.NAME, this.PASS).onConnect(
            new DirList(
                ".",
                new Callback<FTPFile[]>() {
                    public void onReturn(final FTPFile[] files) {
                        for (final FTPFile file : files) {
                            log.info("file: {}", file.toString());
                        }
                    }
                }
            )
        );
    }
}
