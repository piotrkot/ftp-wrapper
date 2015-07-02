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

import com.google.common.base.Charsets;
import com.piokot.ftp.api.Callback;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link FTP} class.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (3 lines)
 * @since 1.0
 */
public final class FTPIntegrationTest {
    /**
     * FTP name name.
     */
    private static final String NAME = "name";
    /**
     * FTP name password.
     */
    private static final String PASS = "pass";
    /**
     * FTP server host.
     */
    private static final String HOST = "localhost";
    /**
     * FTP server port.
     *
     * @checkstyle MagicNumber (2 lines)
     */
    private static final int PORT = 2222;
    /**
     * Temporary directory.
     */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    /**
     * Temporary file.
     */
    private static final Path TEMP = Paths.get(TEMP_DIR, "ftpFile.txt");
    /**
     * FTP server.
     */
    private transient FtpServer server;

    /**
     * Sets up Apache FTP server.
     *
     * @throws Exception When it fails.
     */
    @Before
    public void setUp() throws Exception {
        final UserManager manager = new PropertiesUserManagerFactory()
            .createUserManager();
        final BaseUser user = new BaseUser();
        final ListenerFactory listener = new ListenerFactory();
        final FtpServerFactory factory = new FtpServerFactory();
        user.setName(NAME);
        user.setPassword(PASS);
        user.setHomeDirectory(TEMP_DIR);
        manager.save(user);
        listener.setPort(PORT);
        factory.setUserManager(manager);
        factory.addListener("default", listener.createListener());
        this.server = factory.createServer();
        this.server.start();
        Files.deleteIfExists(TEMP);
    }

    /**
     * Stops FTP server.
     *
     * @throws IOException When it fails.
     */
    @After
    public void cleanUp() throws IOException {
        this.server.stop();
        Files.deleteIfExists(TEMP);
    }

    /**
     * Can list and download an FTP file.
     *
     * @throws Exception If it fails.
     */
    @Test
    public void listAndDownloadFTPFiles() throws Exception {
        final String content = "ftp file content";
        Files.copy(
            new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)), TEMP
        );
        new FTP(HOST, PORT, NAME, PASS).onConnect(
            new DirList(
                ".",
                new Callback<FTPFile[]>() {
                    public void onReturn(final FTPFile[] files) {
                        boolean found = false;
                        for (final FTPFile file : files) {
                            if (TEMP.toFile().getName()
                                .equals(file.getName())) {
                                found = true;
                                break;
                            }
                        }
                        Assert.assertTrue("File not found", found);
                    }
                }
            ),
            new FileDownload(
                TEMP.toFile().getName(),
                new Callback<InputStream>() {
                    @Override
                    @SneakyThrows
                    public void onReturn(final InputStream stream) {
                        Assert.assertEquals(
                            "Content not match",
                            content,
                            new BufferedReader(new InputStreamReader(stream))
                                .readLine()
                        );
                    }
                }
            )
        );
    }

    /**
     * Can call given FTP command and callback during FTP connection.
     *
     * @throws Exception If it fails.
     */
    @Test
    public void callCommands() throws Exception {
        final PassCall<Void> call = new PassCall<>();
        final PassCommand comm = new PassCommand(call);
        new FTP(HOST, PORT, NAME, PASS).onConnect(comm);
        Assert.assertTrue("Called Command", comm.called());
        Assert.assertTrue("Called Callback", call.called());
    }

    /**
     * Callback informing about its execution.
     *
     * @param <T> Result of FTP call.
     */
    class PassCall<T> implements Callback<T> {
        /**
         * Command execution call.
         */
        private transient boolean call;

        @Override
        public void onReturn(final T type) {
            this.call = true;
        }

        /**
         * Test if command was executed.
         *
         * @return True if executed. False, otherwise.
         */
        public boolean called() {
            return this.call;
        }
    }

    /**
     * FTP Command informing about its execution.
     */
    class PassCommand extends AbstractFTPCommand<Void> {
        /**
         * Command execution call.
         */
        private transient boolean call;

        /**
         * Class constructor.
         *
         * @param callback How handle result of type T of FTP call execution.
         */
        public PassCommand(final Callback<Void> callback) {
            super(callback);
        }

        @Override
        public Void ftpCall(final FTPClient client) {
            this.call = true;
            return null;
        }

        /**
         * Test if command was executed.
         *
         * @return True if executed. False, otherwise.
         */
        public boolean called() {
            return this.call;
        }
    }
}
