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

import com.piokot.ftp.api.FTPCommand;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;

/**
 * FTP class being a wrapper around non-OO Apache FTPClient class.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class FTP {
    /**
     * Host to connect to.
     */
    private final transient String hst;
    /**
     * Host's port to connect to.
     */
    private final transient int prt;
    /**
     * User connecting.
     */
    private final transient String usr;
    /**
     * User's password for connection.
     */
    private final transient String pass;

    /**
     * Class constructor.
     *
     * @param host Hostname for FTP connection.
     * @param port Port for FTP connection.
     * @param user User logging to FTP.
     * @param password User password for FTP connection.
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public FTP(
        final String host,
        final int port,
        final String user,
        final String password
    ) {
        this.hst = host;
        this.prt = port;
        this.usr = user;
        this.pass = password;
    }

    /**
     * Main flow executed on FTP connection.
     *
     * @param commands FTP commands run during FTP connection.
     */
    @SneakyThrows
    public void onConnect(final FTPCommand... commands) {
        final FTPClient client = new FTPClient();
        try {
            client.connect(this.hst, this.prt);
            client.login(this.usr, this.pass);
            for (final FTPCommand command : commands) {
                command.execute(client);
            }
        } finally {
            client.logout();
            client.disconnect();
        }
    }
}
