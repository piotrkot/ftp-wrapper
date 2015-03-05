/**
 * Copyright (c) 2015, Piotr Kotlicki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.piokot.ftp;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.piokot.ftp.api.Callback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * Integration tests. Requires FTP server up.
 */
@RunWith(JUnit4.class)
@Slf4j
public class FTPTest {
    @Test
    public void shouldConnect() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new DirList("", new Callback<FTPFile[]>() {
                @Override
                public void onReturn(final FTPFile[] ftpFiles) {
                    for (FTPFile ftpFile : ftpFiles) {
                        log.info(ftpFile.getName());
                    }
                }
            }));
    }

    @Test
    public void shouldUpload() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new FileUpload("TestFTP/testStream",
                new ByteArrayInputStream("stream".getBytes(Charsets.UTF_8)),
                new Callback<Boolean>() {
                    @Override
                    public void onReturn(final Boolean aBoolean) {
                        log.info("Upload success: {}", aBoolean);
                    }
                }
            ), new DirList("TestFTP", new Callback<FTPFile[]>() {
                @Override
                public void onReturn(final FTPFile[] ftpFiles) {
                    for (FTPFile ftpFile : ftpFiles) {
                        log.info(ftpFile.getName());
                        if ("testStream".equals(ftpFile.getName())) {
                            return;
                        }
                    }
                    fail("File testStream does not exist on remote location");
                }
            }));
    }

    @Test
    public void shouldFindRecursively() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new FileSearch("TestFTP", "prefix", true, new Callback<Iterable<String>>() {
                @Override
                public void onReturn(final Iterable<String> findings) {
                    for (String found : findings) {
                        log.info(found);
                    }
                }
            }));
    }

    @Test
    public void shouldDownload() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new FileUpload("TestFTP/testStream",
                new ByteArrayInputStream("stream".getBytes(Charsets.UTF_8)),
                new Callback<Boolean>() {
                    @Override
                    public void onReturn(final Boolean aBoolean) {
                        log.info("Upload success: {}", aBoolean);
                    }
                }
            ), new FileDownload("TestFTP/testStream", new Callback<InputStream>() {
                @Override
                @SneakyThrows
                public void onReturn(final InputStream input) {
                    assertEquals("stream", CharStreams.toString(new InputStreamReader(input, Charsets.UTF_8)));
                }
            }));
    }

    @Test
    public void shouldDelete() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new FileUpload("TestFTP/testStream",
                new ByteArrayInputStream("stream".getBytes(Charsets.UTF_8)),
                new Callback<Boolean>() {
                    @Override
                    public void onReturn(final Boolean aBoolean) {
                        log.info("Upload success: {}", aBoolean);
                    }
                }
            ), new FileDelete("TestFTP/testStream", new Callback<Boolean>() {
                @Override
                public void onReturn(final Boolean result) {
                    log.info("Deleted successful: {}", result);
                }
            }), new DirList("TestFTP", new Callback<FTPFile[]>() {
                @Override
                public void onReturn(FTPFile[] ftpFiles) {
                    for (FTPFile file : ftpFiles) {
                        if ("testStream".equals(file.getName())) {
                            fail("File not deleted");
                        }
                    }
                }
            }));
    }

    @Test
    public void shouldDownloadZip() throws IOException {
        new FTP("localhost", 21, "test", "test").
            onConnect(new FileSearch("TestFTP", "prefix", true, new Callback<Iterable<String>>() {
                @Override
                @SneakyThrows
                public void onReturn(final Iterable<String> findings) {
                    final ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(
                        new FileOutputStream("findings.zip")));
                    for (final String found : findings) {
                        zip.putNextEntry(new ZipEntry(found));
                        new FileDownload(found, new Callback<InputStream>() {
                            @Override
                            @SneakyThrows
                            public void onReturn(final InputStream inputStream) {
                                ByteStreams.copy(inputStream, zip);
                                inputStream.close();
                            }
                        });
                    }
                    zip.close();
                }
            }));
        final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream("findings.zip")));
        ZipEntry next;
        while ((next = zip.getNextEntry()) != null) {
            log.info("Found {}", next.getName());
            assertTrue(next.getName().matches(".*\\/prefix.*"));
        }
    }
}
