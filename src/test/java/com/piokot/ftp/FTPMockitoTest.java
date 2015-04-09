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

import com.piokot.ftp.mock.MockCallback;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Basic mockito tests with mocked Apache FTPClient.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public final class FTPMockitoTest {
    /**
     * Wrapped FTP client.
     */
    @Mock
    private transient FTPClient client;

    /**
     * Should connect to location.
     */
    @Test
    @SneakyThrows
    public void shouldConnect() {
        final String dir = "dir";
        new DirList(dir, new MockCallback<FTPFile[]>()).ftpCall(this.client);
        Mockito.verify(this.client).listFiles(Matchers.matches(dir));
    }

    /**
     * Should Upload simple file containing a string.
     */
    @Test
    @SneakyThrows
    public void shouldUpload() {
        final String loc = "TestFTP/testStream";
        new FileUpload(
            loc,
            new ByteArrayInputStream(
                "stream".getBytes(Charset.forName("UTF-8"))
            ),
            new MockCallback<Boolean>()
        ).ftpCall(this.client);
        Mockito.verify(this.client).storeFile(
            Matchers.matches(loc),
            Matchers.any(InputStream.class)
        );
    }
}
