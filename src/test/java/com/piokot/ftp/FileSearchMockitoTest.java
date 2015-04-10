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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.piokot.ftp.api.Filter;
import com.piokot.ftp.mock.MockCallback;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * Tests for file search.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public final class FileSearchMockitoTest {
    /**
     * Directory outer.
     */
    private static final String DIR_OUT = "dir";
    /**
     * Directory inner.
     */
    private static final String DIR_IN = "dir/dir1";
    /**
     * Prefix.
     */
    private static final String PREFIX = "pre";
    /**
     * Found location.
     */
    private static final String FOUND_LOC = "dir/dir1/pre-test-dir1";
    /**
     * Wrapped FTP client.
     */
    @Mock
    private transient FTPClient client;

    /**
     * Test set up.
     */
    @Before
    @SneakyThrows
    public void setUp() {
        Mockito.when(this.client.listFiles(Matchers.matches(DIR_OUT)))
            .thenAnswer(
                new Answer<FTPFile[]>() {
                    @Override
                    public FTPFile[] answer(final InvocationOnMock inv) {
                        final FTPFile dir = new FTPFile();
                        dir.setName("dir1");
                        dir.setType(FTPFile.DIRECTORY_TYPE);
                        return new FTPFile[]{dir};
                    }
                }
            );
        Mockito.when(this.client.listFiles(Matchers.matches(DIR_IN)))
            .thenAnswer(
                new Answer<FTPFile[]>() {
                    @Override
                    public FTPFile[] answer(final InvocationOnMock inv) {
                        final FTPFile filep = new FTPFile();
                        filep.setName("pre-test-dir1");
                        filep.setType(FTPFile.FILE_TYPE);
                        final FTPFile file = new FTPFile();
                        file.setName("ppp-test-dir1");
                        file.setType(FTPFile.FILE_TYPE);
                        return new FTPFile[]{filep, file};
                    }
                }
            );
    }

    /**
     * Can find file with prefix recursively.
     */
    @Test
    public void findingFileWithPrefixRecursively() {
        final Iterable<String> findings = new FileSearch(
            DIR_OUT,
            new Prefix(PREFIX),
            true,
            new MockCallback<Iterable<String>>()
        ).ftpCall(this.client);
        Assert.assertTrue(
            "found outside",
            Iterables.elementsEqual(ImmutableList.of(FOUND_LOC), findings)
        );
    }

    /**
     * Can find file with prefix non-recursively.
     */
    @Test
    public void findingFileWithPrefixNonRecursively() {
        final Iterable<String> findings = new FileSearch(
            DIR_IN,
            new Prefix(PREFIX),
            false,
            new MockCallback<Iterable<String>>()
        ).ftpCall(this.client);
        Assert.assertTrue(
            "not found inside",
            Iterables.elementsEqual(ImmutableList.of(FOUND_LOC), findings)
        );
    }

    /**
     * Cannot find file in deep when searched non-recursively.
     */
    @Test
    public void findingFileInDeepWhenSearchedNoNRecursively() {
        final Iterable<String> findings = new FileSearch(
            DIR_OUT,
            new Prefix(PREFIX),
            false,
            new MockCallback<Iterable<String>>()
        ).ftpCall(this.client);
        Assert.assertTrue(
            "found something",
            Iterables.elementsEqual(ImmutableList.of(), findings)
        );
    }

    /**
     * Filter on FTPFile that accepts file names with given prefix.
     */
    final class Prefix implements Filter<FTPFile> {
        /**
         * File name prefix.
         */
        private final transient String prfx;
        /**
         * Class constructor.
         *
         * @param prefix Prefix of file name.
         */
        Prefix(final String prefix) {
            this.prfx = prefix;
        }
        @Override
        public boolean valid(final FTPFile file) {
            return file.getName().startsWith(this.prfx);
        }
    }
}
