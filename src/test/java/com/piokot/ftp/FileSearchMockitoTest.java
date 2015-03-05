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

import com.google.common.collect.FluentIterable;
import com.piokot.ftp.mock.MockCallback;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.when;

/**
 * Tests for file search.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileSearchMockitoTest {
    
    @Mock
    private FTPClient ftpClient;
    
    @Before
    public void setup() throws IOException {
        when(ftpClient.listFiles(matches("dir"))).thenAnswer(new Answer<FTPFile[]>() {
            @Override
            public FTPFile[] answer(InvocationOnMock invocationOnMock) throws Throwable {
                FTPFile file1 = new FTPFile();
                file1.setName("pre-test");
                file1.setType(FTPFile.FILE_TYPE);
                FTPFile file2 = new FTPFile();
                file2.setName("ppp-test");
                file2.setType(FTPFile.FILE_TYPE);
                FTPFile dir1 = new FTPFile();
                dir1.setName("dir1");
                dir1.setType(FTPFile.DIRECTORY_TYPE);
                return new FTPFile[]{file1, file2, dir1};
            }
        });
        when(ftpClient.listFiles(matches("dir/dir1"))).thenAnswer(new Answer<FTPFile[]>() {
            @Override
            public FTPFile[] answer(InvocationOnMock invocationOnMock) throws Throwable {
                FTPFile file1 = new FTPFile();
                file1.setName("pre-test-dir1");
                file1.setType(FTPFile.FILE_TYPE);
                FTPFile file2 = new FTPFile();
                file2.setName("ppp-test-dir1");
                file2.setType(FTPFile.FILE_TYPE);
                return new FTPFile[]{file1, file2};
            }
        });
    }
    
    @Test
    public void shouldFindRecursively() throws Exception {
        Iterable<String> findings = new FileSearch("dir", "pre", true,
            new MockCallback<Iterable<String>>()).ftpCall(ftpClient);
        assertEquals(2, FluentIterable.from(findings).size());
        assertEquals("dir/pre-test", FluentIterable.from(findings).get(0));
        assertEquals("dir/dir1/pre-test-dir1", FluentIterable.from(findings).get(1));
    }
    
    @Test
    public void shouldFind() throws Exception {
        Iterable<String> findings = new FileSearch("dir", "pre", false,
            new MockCallback<Iterable<String>>()).ftpCall(ftpClient);
        assertEquals(1, FluentIterable.from(findings).size());
        assertEquals("dir/pre-test", FluentIterable.from(findings).get(0));
    }
    
}